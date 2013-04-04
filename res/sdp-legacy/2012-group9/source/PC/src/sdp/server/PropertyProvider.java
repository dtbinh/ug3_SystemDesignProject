package sdp.server;

import java.util.ArrayList;
import java.util.HashMap;

import sdp.server.math.MathUtils;

public abstract class PropertyProvider {
	private HashMap<String, Property>         properties;
	private HashMap<String, PropertyProvider> subproviders;
	private ArrayList<String>                 localKeys, localAndSubKeys;
	private ArrayList<String>                 subproviderKeys;
	private boolean                           dirtyKeys = true;
	
	public PropertyProvider() {
		properties = new HashMap<String, PropertyProvider.Property>();
		localKeys  = new ArrayList<String>();
		localAndSubKeys = new ArrayList<String>();
		subproviderKeys = new ArrayList<String>();
	}
	
	public ArrayList< String > getAllKeys() {
		if( dirtyKeys )
			updateKeys();

		return localAndSubKeys;
	}
	
	public ArrayList< String > getLocalKeys() {
		return localKeys;
	}
	
	public Property getProperty( String key ) {
		return properties.get( key );
	}
	
	public double getPropertyValue( String key ) {
		Property prop = properties.get( key );
		assert prop != null;
		return prop.getValue();
	}
	
	public void setPropertyValue( String key, double newValue ) {
		Property prop = properties.get( key );
		assert prop != null;
		prop.setValue( MathUtils.clamp( newValue, prop.getMin(), prop.getMax() ) );
	}
	
	public abstract class Property {
		private String key;
		private double min, max, step;
		
		protected Property( String key, double min, double max, double step ) {
			this.key = key;
			this.min = min;
			this.max = max;
			this.step = step;
			
			properties.put( key, this );
			localKeys.add( key );
			
			dirtyKeys = true;
		}
		
		public String getKey() {
			return key;
		}
		
		public double getMin() {
			return min;
		}
		
		public double getMax() {
			return max;
		}
		
		public double getStep() {
			return step;
		}
		
		public abstract void setValue( double newValue );
		public abstract double getValue();
	}
	
	protected class ReflectProperty extends Property {
		private String field;
		
		public ReflectProperty( String key, double min, double max, double step ) {
			this( key, key, min, max, step );
		}
		
		public ReflectProperty( String key, String field, double min, double max, double step ) {
			super( key, min, max, step );
			this.field = field;
		}
		
		@Override
		public void setValue( double newValue ) {
			try {
				PropertyProvider.this.getClass().getField( field ).setDouble( PropertyProvider.this, newValue );
			} catch( Exception e ) {
				System.err.println("PropertyProvider: setValue(): " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		@Override
		public double getValue() {
			try {
				return PropertyProvider.this.getClass().getField( field ).getDouble( PropertyProvider.this );
			} catch( Exception e ) {
				System.err.println("PropertyProvider: getValue(): " + e.getMessage());
				e.printStackTrace();
			}
			
			return Double.NaN;
		}
	}
	
	public void updateKeys() {
		localAndSubKeys.clear();
		localAndSubKeys.addAll( localKeys );
		for( String key : subproviderKeys ) {
			PropertyProvider subprovider = subproviders.get( key );
			ArrayList< String > subkeys = subprovider.getAllKeys();
			for( String subkey : subkeys )
				localAndSubKeys.add( key + "." + subkey );
		}
		dirtyKeys = false;
	}
}
