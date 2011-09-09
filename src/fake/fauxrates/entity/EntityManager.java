package fake.fauxrates.entity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EntityManager {

	private final Map<Class, Map<Long, ? extends Component>> componentStores
		= new ConcurrentHashMap<Class, Map<Long, ? extends Component>>();

	private Set<Long> entities = new HashSet<Long>();
	
	/* singletonization */
	private EntityManager() { }

	private static EntityManager instance = new EntityManager();
	public static EntityManager getInstance() {
		return instance;
	}
	
	private long lastEntity = 0;

	synchronized public long createEntity () {
		entities.add(lastEntity);
		return lastEntity++;
	}
	
	synchronized public void deleteEntity (long entity) {
		entities.remove(entity);
	}
	
	public <T extends Component> void addComponent (long entity, T component) {
		Map<Long, T> h = (Map<Long,T>)componentStores.get(component.getClass());
		
		/* add new component store, synchronized on its type */
		if (h == null) synchronized (component.getClass()) {
			/* maybe someone already added it? */
			h = (Map<Long,T>)componentStores.get(component.getClass());
			if (h == null) { /* nope */
				h = new ConcurrentHashMap<Long, T>();
				/* ConcurrentHashMap componentStores is supposed to handle the 
				 * double-checked lock problem for us */
				componentStores.put(component.getClass(), h);
			}
		}
		
		h.put(entity, component);
	}
	
	public <T extends Component> T getComponent (long entity, Class<T> component) {
		Map<Long, T> m = (Map<Long,T>)componentStores.get(component);
		if (m == null) return null;
		else return m.get(entity);
	}
	
	public <T extends Component> boolean hasComponent (long entity, Class<T> component) {
		Map<Long, T> m = (Map<Long,T>)componentStores.get(component);
		if (m == null) return false; // nobody has the component
		else return m.containsKey(entity);
	}
	
	public <T extends Component> void removeComponent (long entity, Class<T> component) {
		Map<Long, T> m = (Map<Long,T>)componentStores.get(component);
		if (m != null) m.remove(entity);
	}
	
	public <T extends Component> Map<Long, T> getComponentMap (Class<T> component) {
		Map<Long,T> ret = (Map<Long,T>)componentStores.get(component);
		if (ret == null) synchronized (component) {
			ret = (Map<Long,T>)componentStores.get(component);
			if (ret == null) {
				ret = new ConcurrentHashMap<Long, T>();
				componentStores.put(component, ret);
			} /* see addComponent() for explanation */
		}
		return ret;
	}
}
