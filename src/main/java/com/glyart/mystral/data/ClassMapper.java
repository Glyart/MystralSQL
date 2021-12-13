package com.glyart.mystral.data;

import com.glyart.mystral.datasource.DataSourceUtils;
import com.glyart.mystral.exceptions.TypeMismatchException;
import com.glyart.mystral.sql.ResultSetRowMapper;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ResultSetRowMapper implementation that converts a row into a new instance of the specified mapped target class. <br>
 * <p>The mapped target class must be a top-level class and it must have a default or no-arg constructor. </p><br>
 * An optimal structure may be like this:
 * <pre>
 * {@code
 *   public class User {
 *
 *     private int id;
 *     private String name;
 *     private UUID uuid;
 *
 *     public int getId() {
 *         return id;
 *     }
 *
 *     public void setId(int id) {
 *         this.id = id;
 *     }
 *
 *     public String getName() {
 *         return name;
 *     }
 *
 *     @Name(columnName = "username") // this refers to the column name
 *     public void setName(String name) {
 *         this.name = name;
 *     }
 *
 *     public UUID getUuid() {
 *         return uuid;
 *     }
 *
 *     // ClassMapper will try to convert the value obtained from the database to the desired type.
 *     public void setUuid(@MapWith(StringToUUID.class) UUID uuid) {
 *         this.uuid = uuid;
 *     }
 *   }
 * }
 * </pre>
 * <p>Column values are mapped based on matching the column name as obtained from result set meta-data to public setters for the corresponding properties.
 * If field names are different from column names, you can annotate the public setters with the {@link Name} annotation, and specify the column name.</p>
 * <br>
 * <p>Mapping is provided for fields in the target class for many common types, e.g.: String, boolean, Boolean, byte, Byte, short, Short, int, Integer, long, Long, float, Float, double, Double, BigDecimal, java.util.Date, etc.<br></p>
 * <p>If you have to convert a type into a more complex one, you can use converters. With the annotation {@link MapWith}, you can annotate the setter parameter and specify the converter you want to use for that type. <br>
 * Be aware that setters must have 1 and only 1 parameter; they will be ignored otherwise.
 * If you need your own converter, use the {@link Converter} interface for custom implementations.</p>
 * <br>
 * <p>For {@code null} values read from the database, ClassMapper will attempt to call the setter, but in the case of Java primitives, this causes a TypeMismatchException.
 * This class can be configured (using the 'defaultNullValueForPrimitives' property, true by default) to catch this exception and use the primitives default value.<br>
 * <b>ATTENTION: if you use the values from the generated bean to update the database, the primitive value will have been set to the primitive's default value instead of null.</b></p>
 * <p><b>Please note that this class is designed to provide CONVENIENCE rather than high performance.
 * For best performance, consider using a custom ResultSetRowMapper implementation.</b></p><br>
 *
 * @see TypeMismatchException
 * @see ResultSetRowMapper
 * @see Name
 * @see MapWith
 * @see Converter
 */
public class ClassMapper<T> implements ResultSetRowMapper<T> {

    private static final Map<Class<?>, Object> PRIMITIVE_TYPES = ImmutableMap.<Class<?>, Object>builder()
            .put(byte.class, 0)
            .put(short.class, 0)
            .put(int.class, 0)
            .put(long.class, 0)
            .put(float.class, 0.0f)
            .put(double.class, 0.0d)
            .put(boolean.class, false)
            .put(char.class, '\u0000')
            .build();

    // Converter class - Converter user's implementation
    protected static final Map<Class<Converter<Object, Object>>, Converter<Object, Object>> converterMap = new HashMap<>();

    public final Map<Class<?>, Object> primitives() {
        return PRIMITIVE_TYPES;
    }

    protected final Class<T> target;
    protected final PropertyDescriptor[] descriptors;
    protected final Map<String, PropertyDescriptor> descriptorMap; // column names - field descriptors

    private boolean defaultNullValueForPrimitives = true;

    /**
     * Constructs a new ClassMapper which maps the given bean class.
     * @param target the bean class
     */
    public ClassMapper(@NotNull Class<T> target) {
        this.target = Objects.requireNonNull(target, "The target bean class cannot be null.");
        descriptorMap = new HashMap<>();
        try {
            final BeanInfo info = Introspector.getBeanInfo(target);
            this.descriptors = info.getPropertyDescriptors();
        } catch (IntrospectionException e) {
            throw new RuntimeException("Error: cannot introspect on bean " + target.getCanonicalName() + ".", e);
        }
    }

    @Override
    @Nullable
    public T map(@NotNull ResultSet resultSet, int rowNumber) throws SQLException {
        var bean = newInstance(target);

        var descriptors = getDescriptors(target);
        var metaData = resultSet.getMetaData();
        var columnCount = metaData.getColumnCount();
        assignColumnToProperty(metaData, descriptors, columnCount);

        for (int i = 1; i <= columnCount; i++) {
            String columnName = DataSourceUtils.getColumnName(metaData, i);
            PropertyDescriptor descriptor = descriptorMap.get(columnName);
            if (descriptor == null) {
                continue;
            }
            Object value = DataSourceUtils.getResultSetValue(resultSet, i, descriptor.getPropertyType());
            setValue(bean, descriptor, value);
        }
        return bean;
    }

    /**
     * Set whether we're defaulting Java primitives in the case of mapping a null value from corresponding database fields.
     * @return this ClassMapper's instance
     */
    public ClassMapper<T> setDefaultNullValueForPrimitives(boolean defaultNullValueForPrimitives) {
        this.defaultNullValueForPrimitives = defaultNullValueForPrimitives;
        return this;
    }

    /**
     * Return whether we're defaulting Java primitives in the case of mapping a null value from corresponding database fields.
     */
    public boolean isDefaultNullValueForPrimitives() {
        return defaultNullValueForPrimitives;
    }

    /**
     * Adds a new converter for this (and future ones) ClassMapper instance.
     * @param converter the converter implementation
     * @param <S> the type to convert
     * @param <R> the desired type
     * @return this ClassMapper's implementation
     */
    public <S, R> ClassMapper<T> addConverter(Converter<S, R> converter) {
        converterMap.put((Class<Converter<Object, Object>>) converter.getClass(), (Converter<Object, Object>) converter);
        return this;
    }

    protected T newInstance(Class<T> target) throws SQLException {
        try {
            return target.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new SQLException("Cannot create an instance of " + target.getCanonicalName() +
                    ". Does it have a public no-args constructor?", e);
        }
    }

    protected PropertyDescriptor[] getDescriptors(Class<T> target) throws SQLException {
        BeanInfo info;
        try {
            info = Introspector.getBeanInfo(target);
        } catch (IntrospectionException e) {
            throw new SQLException("Error: cannot introspect on bean " + target.getCanonicalName() + ".", e);
        }
        return info.getPropertyDescriptors();
    }

    protected void setValue(T bean, PropertyDescriptor descriptor, Object value) throws TypeMismatchException {
        var setter = descriptor.getWriteMethod();
        if (setter == null || setter.getParameterCount() != 1) {
            return;
        }

        var setterParam = setter.getParameterTypes()[0];
        if (value == null && setterParam.isPrimitive()) {
            if (defaultNullValueForPrimitives) {
                value = PRIMITIVE_TYPES.get(setterParam);
            } else {
                throw new TypeMismatchException("Couldn't set null value to primitive type " + setterParam.getCanonicalName() + ".");
            }
        }

        var converter = retrieveConverter(setter);

        if (converter != null) {
            value = converter.apply(value);
        }

        if (!isCompatible(value, setterParam)) {
            throw new TypeMismatchException("Cannot assign " + value.getClass() + " to " + setter.getClass() + ".");
        }

        try {
            setter.invoke(bean, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    // From https://github.com/apache/commons-dbutils/blob/master/src/main/java/org/apache/commons/dbutils/BeanProcessor.java
    protected void assignColumnToProperty(ResultSetMetaData metaData, PropertyDescriptor[] descriptors, int columnCount) throws SQLException {
        for (int i = 1; i <= columnCount; i++) { // indexes start from 1 when looping over a ResultSet
            String columnName = DataSourceUtils.getColumnName(metaData, i);

            for (PropertyDescriptor descriptor : descriptors) {
                final var method = descriptor.getWriteMethod();
                if (method == null) {
                    continue;
                }
                var propertyName = descriptor.getName();
                final var name = method.getAnnotation(Name.class);
                if (name != null) {
                    propertyName = name.columnName();
                }
                if (propertyName.equalsIgnoreCase(columnName)) {
                    descriptorMap.put(columnName.toLowerCase(), descriptor);
                    break;
                }
            }
        }
    }

    private boolean isCompatible(final Object value, final Class<?> type) {
        return value == null || type.isInstance(value) || isPrimitive(type, value.getClass());
    }

    private boolean isPrimitive(final Class<?> targetType, final Class<?> valueType) {
        if (!targetType.isPrimitive()) {
            return false;
        }

        try {
            final var typeField = valueType.getField("TYPE");
            final var primitiveValueType = typeField.get(valueType);

            if (targetType == primitiveValueType) {
                return true;
            }
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    @Nullable
    protected Converter<Object, Object> retrieveConverter(Method method) {
        MapWith mapWith = retrieveAnnotations(method);
        if (mapWith == null) {
            return null;
        }

        var converterClass = (Class<Converter<Object, Object>>) mapWith.value();
        var converter = converterMap.get(converterClass);

        if (converter == null) {
            try {
                converter = converterClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            addConverter(converter);
        }
        return converter;
    }

    @Nullable
    protected MapWith retrieveAnnotations(Method method) {
        var annotations = method.getParameterAnnotations();
        if (annotations.length == 0) {
            return null;
        }

        var paramAnnotations = annotations[0];
        for (Annotation a : paramAnnotations) {
            if (a instanceof MapWith m) {
                return m;
            }
        }
        return null;
    }

    @NotNull
    public static <S> ClassMapper<S> of(Class<S> type) {
        return new ClassMapper<>(type);
    }
}
