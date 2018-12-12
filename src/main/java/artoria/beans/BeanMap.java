package artoria.beans;

import artoria.converter.TypeConverter;
import artoria.util.Assert;
import artoria.util.ObjectUtils;

import java.util.*;

import static artoria.common.Constants.*;

/**
 * Bean map.
 * @author Kahle
 */
public abstract class BeanMap implements Map, Cloneable {
    private TypeConverter typeConverter;
    private Object bean;

    protected BeanMap() {
    }

    public TypeConverter getTypeConverter() {

        return this.typeConverter;
    }

    public void setTypeConverter(TypeConverter typeConverter) {

        this.typeConverter = typeConverter;
    }

    public Object getBean() {

        return this.bean;
    }

    public void setBean(Object bean) {
        Assert.notNull(bean, "Parameter \"bean\" must not null. ");
        this.bean = bean;
    }

    /**
     * Get the value who property is key's value from bean.
     * @param bean The bean map's bean
     * @param key The bean's property
     * @return The bean's value
     */
    protected abstract Object get(Object bean, Object key);

    /**
     * Put the value who property is key's value from bean.
     * @param bean The bean map's bean
     * @param key The bean's property
     * @param value The bean's value want to put
     * @return The old value about the property
     */
    protected abstract Object put(Object bean, Object key, Object value);

    @Override
    public int size() {

        return this.keySet().size();
    }

    @Override
    public boolean isEmpty() {

        return this.size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {

        return this.keySet().contains(key);
    }

    @Override
    public boolean containsValue(Object value) {
        Set keys = this.keySet();
        for (Object key : keys) {
            Object thisVal = this.get(key);
            boolean b = value == null && thisVal == null;
            b = b || (value != null && value.equals(thisVal));
            if (b) { return true; }
        }
        return false;
    }

    @Override
    public Object get(Object key) {

        return this.get(this.bean, key);
    }

    @Override
    public Object put(Object key, Object value) {

        return this.put(this.bean, key, value);
    }

    @Override
    public Object remove(Object key) {

        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map m) {
        Set keys = this.keySet();
        for (Object key : keys) {
            this.put(key, m.get(key));
        }
    }

    @Override
    public void clear() {

        throw new UnsupportedOperationException();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Collection values() {
        Set keys = this.keySet();
        List values = new ArrayList(keys.size());
        for (Object key : keys) {
            values.add(this.get(key));
        }
        return Collections.unmodifiableCollection(values);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Set<Entry> entrySet() {
        Set keys = this.keySet();
        int size = keys.size();
        HashMap copy =
                new HashMap(size);
        for (Object key : keys) {
            copy.put(key, this.get(key));
        }
        return Collections.unmodifiableMap(copy).entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Map)) {
            return false;
        }
        Map other = (Map) o;
        if (this.size() != other.size()) {
            return false;
        }
        Set keys = this.keySet();
        for (Object key : keys) {
            if (!other.containsKey(key)) {
                return false;
            }
            Object thisVal = this.get(key);
            Object otherVal = other.get(key);
            if (!ObjectUtils.equals(thisVal, otherVal)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int code = 0;
        Set keys = this.keySet();
        for (Object key : keys) {
            Object val = this.get(key);
            int keyCode = key == null ? 0 : key.hashCode();
            int valCode = val == null ? 0 : val.hashCode();
            code += keyCode ^ valCode;
        }
        return code;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(LEFT_CURLY_BRACKET);
        Set keys = this.keySet();
        for (Object key : keys) {
            builder.append(key);
            builder.append(EQUAL);
            builder.append(this.get(key));
            builder.append(COMMA);
            builder.append(BLANK_SPACE);
        }
        if (!keys.isEmpty()) {
            builder.deleteCharAt(builder.length() - 1);
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append(RIGHT_CURLY_BRACKET);
        return builder.toString();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        BeanMap newMap = (BeanMap) super.clone();
        if (newMap.getBean() == null) {
            return newMap;
        }
        Object bean = this.getBean();
        Object clone = BeanUtils.clone(bean);
        newMap.setBean(clone);
        return newMap;
    }

}