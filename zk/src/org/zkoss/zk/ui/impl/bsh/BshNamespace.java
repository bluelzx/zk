/* BshNamespace.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mon Apr 10 16:08:23     2006, Created by tomyeh
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zk.ui.impl.bsh;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

import bsh.NameSpace;
import bsh.UtilEvalError;
import bsh.Primitive;
import bsh.BshMethod;

import org.zkoss.lang.D;
import org.zkoss.lang.Classes;
import org.zkoss.lang.Objects;
import org.zkoss.util.logging.Log;

import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.util.Namespace;

/**
 * An implementation of {@link Namespace} on top of BeanShell.
 *
 * @author tomyeh
 */
public class BshNamespace implements Namespace {//not a good idea to serialize it
	private static final Log log = Log.lookup(BshNamespace.class);

	private Namespace _parent;
	private NameSpace _ns;
	/** A stack of backup-ed variables List(Map(String name, Object val)). */
	private List _backups;

	/** Constructs a name space of a component (ID space owner).
	 */
	public BshNamespace(String id) {
		_ns = new LiteNameSpace(null, id);
	}
	/** Constructs a name space for a page.
	 */
	public BshNamespace(NameSpace ns) {
		if (ns == null)
			throw new IllegalArgumentException("null");
		_ns = ns;
	}
 
 	//-- Namespace --//
	public Class getClass(String clsnm) throws ClassNotFoundException {
		try {
			final Class cls = _ns.getClass(clsnm);
			if (cls == null)
				throw new ClassNotFoundException("Class not found: "+clsnm);
			return cls;
		} catch (UtilEvalError ex) {
			throw new ClassNotFoundException("Failed to load class "+clsnm);
		}
			
	}
 	public Object getVariable(String name, boolean local) {
		final NameSpace oldp = local ? _ns.getParent(): null;
		if (oldp != null) _ns.setParent(null); //to avoid calling parent's getVariable
 		try {
	 		return Primitive.unwrap(_ns.getVariable(name));
		} catch (UtilEvalError ex) {
			throw UiException.Aide.wrap(ex);
		} finally {
			if (oldp != null) _ns.setParent(oldp); //restore
		}
 	}
	public void setVariable(String name, Object value, boolean local) {
		final NameSpace oldp = local ? _ns.getParent(): null;
		if (oldp != null) _ns.setParent(null); //to avoid calling parent's setVariable
		try {
			_ns.setVariable(name, value, false);
		} catch (UtilEvalError ex) {
			throw UiException.Aide.wrap(ex);
		} finally {
			if (oldp != null) _ns.setParent(oldp); //restore
		}
	}
	public void unsetVariable(String name) {
		_ns.unsetVariable(name);
	}
	public Namespace getParent() {
		return _parent;
	}
	public void setParent(Namespace parent) {
		if (!Objects.equals(_parent, parent)) {
			_parent = parent;
			_ns.setParent(parent != null ? 
				(NameSpace)parent.getNativeNamespace(): null);
		}
	}

	public Object getNativeNamespace() {
		return _ns;
	}

	public void copy(Namespace from, Filter filter) {
		if (!(from instanceof BshNamespace))
			throw new UnsupportedOperationException("BshNamespace to BshNamespace only: "+from);

		final NameSpace bshfrom = ((BshNamespace)from)._ns;

		//variables
		try {
			final String[] vars = bshfrom.getVariableNames();
			for (int j = vars != null ? vars.length: 0; --j >= 0;) {
				final String nm = vars[j];
				if (!"bsh".equals(nm)) {
					final Object val = from.getVariable(nm, true);
					if (filter == null || filter.accept(nm, val))
						setVariable(nm, val, true);
				}
			}

			//methods
			final BshMethod[] mtds = bshfrom.getMethods();
			for (int j = mtds != null ? mtds.length: 0; --j >= 0;) {
				final String nm = mtds[j].getName();
				if (filter == null || filter.accept(nm, mtds[j]))
					_ns.setMethod(nm, mtds[j]);
			}
		} catch (UtilEvalError ex) {
			throw UiException.Aide.wrap(ex);
		}
	}
	public void write(java.io.ObjectOutputStream s, Filter filter)
	throws java.io.IOException {
		//1. variables
		final String[] vars = _ns.getVariableNames();
		for (int j = vars != null ? vars.length: 0; --j >= 0;) {
			final String nm = vars[j];
			if (nm != null && !"bsh".equals(nm)) {
				final Object val = getVariable(nm, true);
				//we cannot store null value since setVariable won't accept it
				if (((val instanceof java.io.Serializable)
					|| (val instanceof java.io.Externalizable))
				&& (filter == null || filter.accept(nm, val))) {
					s.writeObject(nm);
					s.writeObject(val);
				}
			}
		}
		s.writeObject(null); //denote end-of-vars

		//2. methods
		final BshMethod[] mtds = _ns.getMethods();
		for (int j = mtds != null ? mtds.length: 0; --j >= 0;) {
			final String nm = mtds[j].getName();
			if (filter == null || filter.accept(nm, mtds[j])) {
				//hack BeanShell 2.0b4 which cannot be serialized correctly
				Field f = null;
				boolean acs = false;
				try {
					f = Classes.getAnyField(BshMethod.class, "declaringNameSpace");
					acs = f.isAccessible();
					f.setAccessible(true);
					final Object old = f.get(mtds[j]);
					try {
						f.set(mtds[j], null);				
						s.writeObject(mtds[j]);
					} finally {
						f.set(mtds[j], old);
					}
				} catch (java.io.IOException ex) {
					throw ex;
				} catch (Throwable ex) {
					throw UiException.Aide.wrap(ex);
				} finally {
					if (f != null) f.setAccessible(acs);
				}
			}
		}
		s.writeObject(null); //denote end-of-mtds

		//3. imported class
		Field f = null;
		boolean acs = false;
		try {
			f = Classes.getAnyField(NameSpace.class, "importedClasses");
			acs = f.isAccessible();
			f.setAccessible(true);
			final Map clses = (Map)f.get(_ns);
			if (clses != null)
				for (Iterator it = clses.values().iterator(); it.hasNext();) {
					final String clsnm = (String)it.next();
					if (!clsnm.startsWith("bsh."))
						s.writeObject(clsnm);
				}
		} catch (java.io.IOException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw UiException.Aide.wrap(ex);
		} finally {
			if (f != null) f.setAccessible(acs);
		}
		s.writeObject(null); //denote end-of-cls

		//4. imported package
		f = null;
		acs = false;
		try {
			f = Classes.getAnyField(NameSpace.class, "importedPackages");
			acs = f.isAccessible();
			f.setAccessible(true);
			final Collection pkgs = (Collection)f.get(_ns);
			if (pkgs != null)
				for (Iterator it = pkgs.iterator(); it.hasNext();) {
					final String pkgnm = (String)it.next();
					if (!pkgnm.startsWith("java.awt")
					&& !pkgnm.startsWith("javax.swing"))
						s.writeObject(pkgnm);
				}
		} catch (java.io.IOException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw UiException.Aide.wrap(ex);
		} finally {
			if (f != null) f.setAccessible(acs);
		}
		s.writeObject(null); //denote end-of-cls
	}
	public void read(java.io.ObjectInputStream s)
	throws java.io.IOException, ClassNotFoundException {
		for (;;) {
			final String nm = (String)s.readObject();
			if (nm == null) break; //no more

			setVariable(nm, s.readObject(), true);
		}

		try {
			for (;;) {
				final BshMethod mtd = (BshMethod)s.readObject();
				if (mtd == null) break; //no more

				//fix declaringNameSpace
				Field f = null;
				boolean acs = false;
				try {
					f = Classes.getAnyField(BshMethod.class, "declaringNameSpace");
					acs = f.isAccessible();
					f.setAccessible(true);
					f.set(mtd, _ns);				
				} catch (Throwable ex) {
					throw UiException.Aide.wrap(ex);
				} finally {
					if (f != null) f.setAccessible(acs);
				}

				_ns.setMethod(mtd.getName(), mtd);
			}
		} catch (UtilEvalError ex) {
			throw UiException.Aide.wrap(ex);
		}

		for (;;) {
			final String nm = (String)s.readObject();
			if (nm == null) break; //no more

			_ns.importClass(nm);
		}

		for (;;) {
			final String nm = (String)s.readObject();
			if (nm == null) break; //no more

			_ns.importPackage(nm);
		}
	}

	public void backupVariable(String name, boolean newBlock) {
		if (newBlock || _backups == null || _backups.isEmpty()) {
			if (_backups == null) _backups = new LinkedList();
			_backups.add(0, new HashMap());
		}
		if (name != null) {
			final Map map = (Map)_backups.get(0);
			map.put(name, getVariable(name, true));
		}
	}
	public void restoreVariables() {
		if (_backups == null || _backups.isEmpty()) {
			log.warning("restore without any block of backup-ed variables, "+this);
		} else {
			final Map map = (Map)_backups.remove(0);
			for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
				final Map.Entry me = (Map.Entry)it.next();
				final String name = (String)me.getKey();
				final Object val = me.getValue();
				//if (D.ON && log.finerable()) log.finer("Restore "+name+"="+val);
				if (val != null) setVariable(name, val, true);
				else unsetVariable(name);
			}
		}
	}

	//-- Object --//
	public int hashCode() {
		return _ns.hashCode();
	}
	public boolean equals(Object o) {
		return o instanceof BshNamespace && ((BshNamespace)o)._ns.equals(_ns);
	}
}
