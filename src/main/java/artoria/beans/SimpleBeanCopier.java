package artoria.beans;

import artoria.convert.TypeConverter;
import artoria.exception.ExceptionUtils;
import artoria.logging.Logger;
import artoria.logging.LoggerFactory;
import artoria.reflect.ReflectUtils;
import artoria.util.ArrayUtils;
import artoria.util.Assert;

import java.lang.reflect.Method;
import java.util.Map;

import static artoria.common.Constants.ZERO;

/**
 * Bean copier simple implement by jdk.
 * @author Kahle
 */
public class SimpleBeanCopier implements BeanCopier {
    private static Logger log = LoggerFactory.getLogger(SimpleBeanCopier.class);
    private Boolean ignoreException = true;

    public Boolean getIgnoreException() {

        return ignoreException;
    }

    public void setIgnoreException(Boolean ignoreException) {
        Assert.notNull(ignoreException, "Parameter \"ignoreException\" must not null. ");
        this.ignoreException = ignoreException;
    }

    @Override
    public void copy(Object from, Object to, TypeConverter converter) {
        Assert.notNull(from, "Parameter \"from\" must is not null. ");
        Assert.notNull(to, "Parameter \"to\" must is not null. ");
        boolean hasCvt = converter != null;
        Class<?> fromClass = from.getClass();
        Class<?> toClass = to.getClass();
        Map<String, Method> fromMths = ReflectUtils.findReadMethods(fromClass);
        Map<String, Method> toMths = ReflectUtils.findWriteMethods(toClass);
        for (Map.Entry<String, Method> entry : fromMths.entrySet()) {
            String name = entry.getKey();
            Method destMth = toMths.get(name);
            if (destMth == null) { continue; }
            Method srcMth = entry.getValue();
            Class<?>[] types = destMth.getParameterTypes();
            try {
                boolean haveType = ArrayUtils.isNotEmpty(types);
                Object input = srcMth.invoke(from);
                if (input == null && haveType
                        && types[ZERO].isPrimitive()) {
                    throw new NullPointerException();
                }
                if (hasCvt && haveType) {
                    input = converter.convert(input, types[ZERO]);
                }
                destMth.invoke(to, input);
            }
            catch (Exception e) {
                if (ignoreException) {
                    log.debug("Execution \"copy\" error. ", e);
                }
                else {
                    throw ExceptionUtils.wrap(e);
                }
            }
        }
    }

}
