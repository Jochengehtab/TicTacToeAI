package src.SPSA;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class EngineParamApplier {
    public static ParamApplier create() {
        return (search, board, param) -> {
            String name = param.name;
            int value = param.get();

            if (search instanceof TunableSearch ts) {
                if ("lmrBase".equalsIgnoreCase(name)) {
                    ts.setLmrBase(value);
                    return;
                }
                if ("lmrDivisor".equalsIgnoreCase(name)) {
                    ts.setLmrDivisor(value);
                    return;
                }
            }

            // Try setter set<Name>(int)
            String setter = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
            try {
                Method m = search.getClass().getMethod(setter, int.class);
                m.invoke(search, value);
                return;
            } catch (NoSuchMethodException ignored) {
            } catch (Exception e) {
                throw new RuntimeException("Setter " + setter + "(int) failed: " + e.getMessage(), e);
            }

            // Try public field <name>
            try {
                Field f = search.getClass().getField(name);
                if (f.getType() == int.class) {
                    f.setInt(search, value);
                    return;
                }
            } catch (NoSuchFieldException ignored) {
            } catch (Exception e) {
                throw new RuntimeException("Public field " + name + " failed: " + e.getMessage(), e);
            }

            // Try declared field <name>
            try {
                Field f = search.getClass().getDeclaredField(name);
                f.setAccessible(true);
                if (f.getType() == int.class) {
                    f.setInt(search, value);
                    return;
                }
            } catch (NoSuchFieldException ignored) {
            } catch (Exception e) {
                throw new RuntimeException("Private field " + name + " failed: " + e.getMessage(), e);
            }

            throw new IllegalArgumentException(
                    "Cannot apply param '" + name + "': add a setter/field, or use lmrBase/lmrDivisor.");
        };
    }
}