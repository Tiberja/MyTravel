package com.example.mytravel.worldmap;

import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

public class VectorUtil {

    public static boolean setPathFillColor(Drawable drawable, String targetName, @ColorInt int color) {
        if (drawable == null || targetName == null) return false;

        try {
            Object vectorState = getField(drawable, "mVectorState");
            if (vectorState == null) return false;

            Object renderer = getField(vectorState, "mVPathRenderer");
            if (renderer == null) return false;

            // ✅ 1) Best case: direkte Lookup-Map nach Namen (stabiler als Tree-Walk)
            Object vgTargets = tryGetField(renderer, "mVGTargets");
            if (vgTargets instanceof Map) {
                Object targetObj = ((Map<?, ?>) vgTargets).get(targetName);
                if (targetObj != null) {
                    boolean ok = setFillOnAnyPathObject(targetObj, color);
                    if (ok) {
                        drawable.invalidateSelf();
                        return true;
                    }
                }
            }

            // ✅ 2) Fallback: Tree Walk (wie vorher, aber Name-Felder flexibler)
            Object rootGroup = getField(renderer, "mRootGroup");
            if (rootGroup == null) return false;

            boolean ok = walkAndColor(rootGroup, targetName, color);
            if (ok) drawable.invalidateSelf();
            return ok;

        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    private static boolean walkAndColor(Object group, String targetName, int color) throws Exception {
        Object childrenObj = getField(group, "mChildren");
        if (!(childrenObj instanceof ArrayList)) return false;

        ArrayList<?> children = (ArrayList<?>) childrenObj;

        for (Object child : children) {
            String name = firstNonNull(
                    tryGetStringField(child, "mPathName"),
                    tryGetStringField(child, "mName")
            );

            if (targetName.equals(name)) {
                return setFillOnAnyPathObject(child, color);
            }

            Object maybeChildren = tryGetField(child, "mChildren");
            if (maybeChildren instanceof ArrayList) {
                if (walkAndColor(child, targetName, color)) return true;
            }
        }
        return false;
    }

    private static boolean setFillOnAnyPathObject(Object obj, int color) {
        try {
            // A) mFillColor direkt int/Integer
            Object fill = tryGetField(obj, "mFillColor");
            if (fill instanceof Integer) {
                setField(obj, "mFillColor", color);
                return true;
            }

            // B) AndroidX: ComplexColorCompat -> mColor
            if (fill != null) {
                Object inner = tryGetField(fill, "mColor");
                if (inner instanceof Integer) {
                    setField(fill, "mColor", color);
                    return true;
                }
            }

            // C) Paint-Fallback
            Object fillPaint = tryGetField(obj, "mFillPaint");
            if (fillPaint instanceof Paint) {
                ((Paint) fillPaint).setColor(color);
                return true;
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }

    // ---------------- helpers ----------------

    private static Object getField(Object obj, String fieldName) throws Exception {
        Field f = findField(obj.getClass(), fieldName);
        return f.get(obj);
    }


    private static Object tryGetField(Object obj, String fieldName) {
        try { return getField(obj, fieldName); }
        catch (Throwable t) { return null; }
    }

    private static void setField(Object obj, String fieldName, Object value) throws Exception {
        Field f = findField(obj.getClass(), fieldName);
        f.set(obj, value);
    }


    private static String tryGetStringField(Object obj, String fieldName) {
        try {
            Object v = getField(obj, fieldName);
            return (v instanceof String) ? (String) v : null;
        } catch (Throwable t) {
            return null;
        }
    }

    private static String firstNonNull(String a, String b) {
        return a != null ? a : b;
    }

    private static Field findField(Class<?> cls, String fieldName) throws NoSuchFieldException {
        Class<?> c = cls;
        while (c != null) {
            try {
                Field f = c.getDeclaredField(fieldName);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException ignore) {
                c = c.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    public static void debugDumpNames(Drawable drawable) {
        try {
            Object vectorState = getField(drawable, "mVectorState");
            Object renderer = getField(vectorState, "mVPathRenderer");

            Object vgTargets = tryGetField(renderer, "mVGTargets");
            if (vgTargets instanceof java.util.Map) {
                android.util.Log.e("WorldMapDebug", "mVGTargets keys=" + ((java.util.Map<?, ?>) vgTargets).keySet());
            } else {
                android.util.Log.e("WorldMapDebug", "mVGTargets not a Map: " + (vgTargets == null ? "null" : vgTargets.getClass().getName()));
            }

            Object rootGroup = getField(renderer, "mRootGroup");
            android.util.Log.e("WorldMapDebug", "Tree names=" + collectNames(rootGroup));

        } catch (Throwable t) {
            android.util.Log.e("WorldMapDebug", "debugDumpNames failed: " + t);
            t.printStackTrace();
        }
    }

    private static java.util.List<String> collectNames(Object group) throws Exception {
        java.util.ArrayList<String> out = new java.util.ArrayList<>();
        Object childrenObj = tryGetField(group, "mChildren");
        if (!(childrenObj instanceof java.util.ArrayList)) return out;

        for (Object child : (java.util.ArrayList<?>) childrenObj) {
            String name = firstNonNull(
                    tryGetStringField(child, "mPathName"),
                    tryGetStringField(child, "mName")
            );
            if (name != null) out.add(name);

            Object maybeChildren = tryGetField(child, "mChildren");
            if (maybeChildren instanceof java.util.ArrayList) {
                out.addAll(collectNames(child));
            }
        }
        return out;
    }


}
