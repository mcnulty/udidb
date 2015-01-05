package net.udidb.expr.lang.c;

import java.util.HashMap;
import java.util.Map;

import net.sourcecrumbs.api.debug.symbols.DebugType;

/**
 * Container for C Language type constants and helper methods
 *
 * @author dmcnulty
 */
public final class Types
{
    /* type names */
    public static final String UNSIGNED_INT_NAME = "unsigned int";
    public static final String UNSIGNED_LONG_NAME = "unsigned long int";
    public static final String UNSIGNED_LONG_LONG_NAME = "unsigned long long int";
    public static final String SIGNED_INT_NAME = "signed int";
    public static final String SIGNED_LONG_NAME = "signed long int";
    public static final String SIGNED_LONG_LONG_NAME = "signed long long int";

    public static final String FLOAT_NAME = "float";
    public static final String DOUBLE_NAME = "double";
    public static final String LONG_DOUBLE_NAME = "long double";

    public static final String CHAR_NAME = "char";
    public static final String WIDE_CHAR_NAME = "wchar_t";
    public static final String CHAR16_NAME = "char16_t";
    public static final String CHAR32_NAME = "char32_t";

    public static final String VOID_NAME = "void";

    public static DebugType getType(String name)
    {
        return getType(name, false);
    }

    public static DebugType getType(String name, boolean pointer)
    {
        ExpressionConstantType type;
        synchronized (expressionConstantTypeCache) {
            type = expressionConstantTypeCache.get(name);
            if (type == null) {
                // Need to create the type
                type = new ExpressionConstantType();
                String actualName = name;
                if (pointer) {
                    actualName += " *";
                }
                type.setName(actualName);
                type.setPointer(pointer);
                expressionConstantTypeCache.put(name, type);
            }
        }
        return type;
    }

    public static boolean equals(DebugType left, DebugType right)
    {
        return (left.getName().equals(right.getName())) &&
            (left.isPointer() == right.isPointer()) &&
            (left.isImmutable() == right.isImmutable());
    }

    public static boolean isFloatType(DebugType type)
    {
        switch(type.getName()) {
            case FLOAT_NAME:
            case DOUBLE_NAME:
            case LONG_DOUBLE_NAME:
                return true;
            default:
                break;
        }

        return false;
    }

    public static boolean isSignedIntegerType(DebugType type)
    {
        switch(type.getName()) {
            case SIGNED_INT_NAME:
            case SIGNED_LONG_NAME:
            case SIGNED_LONG_LONG_NAME:
                return true;
            default:
                break;
        }

        return false;
    }

    public static boolean isUnsignedIntegerType(DebugType type)
    {
        switch(type.getName()) {
            case UNSIGNED_INT_NAME:
            case UNSIGNED_LONG_NAME:
            case UNSIGNED_LONG_LONG_NAME:
                return true;
            default:
                break;
        }

        return false;
    }

    public static boolean isIntegerType(DebugType type)
    {
        return isSignedIntegerType(type) || isUnsignedIntegerType(type);
    }

    public static DebugType getIntegerSuffixType(String suffix)
    {
        switch(suffix)
        {
            case "u":
            case "U":
                return getType(UNSIGNED_INT_NAME);
            case "l":
            case "L":
                return getType(SIGNED_LONG_NAME);
            case "ll":
            case "LL":
                return getType(SIGNED_LONG_LONG_NAME);
            case "ul":
            case "Ul":
            case "uL":
            case "UL":
            case "lu":
            case "lU":
            case "Lu":
            case "LU":
                return getType(UNSIGNED_LONG_NAME);
            case "ull":
            case "Ull":
            case "uLL":
            case "ULL":
            case "llu":
            case "llU":
            case "LLu":
            case "LLU":
                return getType(UNSIGNED_LONG_LONG_NAME);
            default:
                break;
        }

        // This should trigger a syntax error and hence type analysis shouldn't occur if syntax errors exist
        throw new IllegalArgumentException("Unknown integer suffix " + suffix);
    }

    public static DebugType getFloatSuffixType(String suffix)
    {
        switch(suffix) {
            case "f":
            case "F":
                return getType(FLOAT_NAME);
            case "l":
            case "L":
                return getType(LONG_DOUBLE_NAME);
            default:
                break;
        }

        // This should trigger a syntax error and hence type analysis shouldn't occur if syntax errors exist
        throw new IllegalArgumentException("Unknown float suffix " + suffix);
    }

    private static final Map<String, ExpressionConstantType> expressionConstantTypeCache = new HashMap<>();

    private static class ExpressionConstantType implements DebugType
    {
        private String name;

        private boolean pointer;

        public void setName(String name)
        {
            this.name = name;
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public boolean isPointer()
        {
            return pointer;
        }

        @Override
        public boolean isMemberStructure()
        {
            return false;
        }

        @Override
        public DebugType[] getMemberTypes()
        {
            return new DebugType[0];
        }

        public void setPointer(boolean pointer)
        {
            this.pointer = pointer;
        }

        @Override
        public boolean isImmutable()
        {
            return true;
        }

        @Override
        public DebugType getBaseType()
        {
            return null;
        }
    }
}
