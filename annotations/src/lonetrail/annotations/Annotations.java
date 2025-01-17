package lonetrail.annotations;

import arc.func.*;
import arc.struct.*;
import com.squareup.javapoet.*;
import mindustry.world.*;

import java.lang.annotation.*;

public class Annotations {
    /** Indicates that this content's entity type inherits interfaces */
    @Target({ElementType.FIELD, ElementType.TYPE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EntityDef {
        /** @return The interfaces that will be inherited by the generated entity class */
        Class<?>[] value();

        /** @return Whether the class can serialize itself */
        boolean serialize() default true;

        /** @return Whether the class can write/read to/from save files */
        boolean genio() default true;

        /** @return Whether the class is poolable */
        boolean pooled() default false;
    }

    /** Indicates that this content's entity will be the one that is pointed, or if it's the type it will get mapped to the entity mapping */
    @Target({ElementType.FIELD, ElementType.TYPE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EntityPoint {
        /** @return The entity type */
        Class<?> value() default Void.class;
    }

    /** Works somewhat like {@code Object.assign(...)} for Block and Building */
    @Target({ElementType.TYPE, ElementType.FIELD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Merge {
        /** @return The base class */
        Class<?> base() default Block.class;

        /** @return The merged classes */
        Class<?>[] value();
    }

    /** Notifies that this class is a component class; an interface will be generated out of this */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface MergeComponent {}

    /** The generated interface from {@link MergeComponent} */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface MergeInterface {}

    /** Works somewhat like Ctrl CV for Block and Building */
    @Target({ElementType.TYPE, ElementType.FIELD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Dupe {
        /** @return The base class and code */
        Class<?> base() default Block.class;

        /** @return The new parent of the adopted base */
        Class<?> parent();

        /** @return The duped target name */
        String name() default "";
    }

    /** Notifies that this class is a component class; an interface will be generated out of this */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface DupeComponent {}

    /** The generated interface from {@link DupeComponent} */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface DupeInterface {}

    /** Indicates that {@link Dupe} should ignore the field or method completely */
    @Target({ElementType.FIELD, ElementType.METHOD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Ignore {}

    /** Indicates that {@link Dupe} should replace the method completely */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Actually {
        /** @return The replacement */
        String value() default "";
    }

    /** Indicates that this class is an entity component */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface EntityComponent {
        /** @return Whether the component should be interpreted into interfaces */
        boolean write() default true;

        /** @return Whether the component should generate a base class for itself */
        boolean base() default false;
    }

    /** All entity components will inherit from this */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface EntityBaseComponent {}

    /** Whether this interface wraps an entity component */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface EntityInterface {}

    /** Fills a {@code Seq.<String>with()}'s arg with the list of compiled classes with their qualified names. */
    @Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ListClasses {}

    /** Fills a {@code Seq.<String>with()}'s arg with the list of compiled packages. */
    @Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ListPackages {}

    /** Prevents this component from getting added into an entity group, specified by the group's element type */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface ExcludeGroups {
        /** @return The excluded group's element type */
        Class<?>[] value();
    }

    /** Generates value-type wrapper for this class */
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Struct {}

    /** Defines a size and optional float packer for a struct field */
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface StructField {
        /** @return The size of this field. 0 marks it as default size. When dealing with floats, this is replaced with {@link FloatPacker#size} */
        int value() default 0;

        /** @return The name of a struct-wrapper field. Must not be empty */
        String name() default "";

        /** @return The float packer of this field, if applicable */
        FloatPacker packer() default FloatPacker.def;

        /** Float packers */
        enum FloatPacker {
            /** Default. Takes all 32 bits */
            def(32,
                    f -> "Float.floatToIntBits(" + f + ")",
                    f -> "Float.intBitsToFloat(" + f + ")"
            ),

            /** RGBA8888 color format. Takes 8 bits for typically 4 floats each */
            rgba8888(8,
                    f -> "(" + f + " * 255f)",
                    f -> "(" + f + " / 255f)"
            );

            public final int size;
            public final Func<String, String> packer;
            public final Func<String, String> unpacker;

            FloatPacker(int size, Func<String, String> packer, Func<String, String> unpacker) {
                this.size = size;
                this.packer = packer;
                this.unpacker = unpacker;
            }
        }
    }

    /** Wraps an existing value class so that it can be packed into {@code int}s / {@code long}s */
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface StructWrap {
        /** @return The fields that are taken into account */
        StructField[] value();

        /** @return Whether the bits start from left-most */
        boolean left() default false;
    }

    /** Indicates that a field will be interpolated when synced. */
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SyncField {
        /** If true, the field will be linearly interpolated. If false, it will be interpolated as an angle. */
        boolean value();

        /** If true, the field is clamped to 0-1. */
        boolean clamped() default false;
    }

    /** Indicates that a field will not be read from the server when syncing the local player state. */
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SyncLocal {}

    /** Indicates that a field should not be synced to clients (but may still be non-transient) */
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface NoSync {}

    /** Indicates that the field annotated with this came from another component class */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Import {}

    /** Whether the field returned by this getter is meant to be read-only */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface ReadOnly {}

    /** Whether this method replaces the actual method in the base class */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Replace {
        /** @return The priority of this replacer, in case of non-void methods */
        int value() default 0;
    }

    /** Whether this method is implemented in annotation-processing time */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface InternalImpl {}

    /** Used for method appender sorting */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface MethodPriority {
        /** @return The priority */
        int value();
    }

    /** Inserts this parameter-less method into another void method */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Insert {
        /**
         * @return The target method described in {@link String} with the format {@code <methodName>(<paramType>...)}.
         * For example, when targetting {@code void call(String arg, int prior)}, the target descriptor must be
         * {@code call(java.lang.String, int)}
         */
        String value();

        /** @return The component-specific method implementation to target */
        Class<?> block() default Void.class;

        /** @return Whether the call to this method is after the default or not */
        boolean after() default true;
    }

    /** Wraps a component-specific method implementation with this boolean parameterless method */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Wrap {
        /**
         * @return The target method described in {@link String} with the format {@code <methodName>(<paramType>...)}.
         * For example, when targetting {@code void call(String arg, int prior)}, the target descriptor must be
         * {@code call(java.lang.String, int)}
         */
        String value();

        /** @return The component-specific method implementation to target */
        Class<?> block() default Void.class;
    }

    /** Appends this {@code add()}/{@code remove()} method before the {@code if([!]added)} check */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface BypassGroupCheck {}

    /** Will not replace {@code return;} to {@code break [block];}, hence breaking the entire method statement */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface BreakAll {}

    /** Removes a component-specific method implementation */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Remove {
        Class<?> value();
    }

    /** States that this method is only relevant on types with a certain entity component */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Extend {
        Class<?> value();
    }

    /** Resolves how to handle multiple non-void method specifications. */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Combine {}

    /** To be used with {@link Combine}. */
    @Target(ElementType.LOCAL_VARIABLE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Resolve {
        Method value();

        enum Method {
            // Numeric values.
            add(false, (vars, cons) -> {
                if (vars.size == 1) {
                    cons.get("$L", Seq.with(vars.first()));
                    return;
                }

                StringBuilder format = new StringBuilder();
                format.append((format.isEmpty() ? "$L" : " + $L").repeat(Math.max(0, vars.size)));

                cons.get(format.toString(), vars);
            }),
            average(false, (vars, cons) -> {
                if (vars.size == 1) {
                    cons.get("$L", Seq.with(vars.first()));
                    return;
                }

                StringBuilder format = new StringBuilder();
                format.append((format.isEmpty() ? "$L" : " + $L").repeat(Math.max(0, vars.size)));

                format.insert(0, "(").append(") / $L");
                cons.get(format.toString(), Seq.with(vars).add(String.valueOf(vars.size)));
            }),
            multiply(false, (vars, cons) -> {
                if (vars.size == 1) {
                    cons.get("$L", Seq.with(vars.first()));
                    return;
                }

                StringBuilder format = new StringBuilder();
                format.append((format.isEmpty() ? "$L" : " + $L").repeat(Math.max(0, vars.size)));

                cons.get(format.toString(), vars);
            }),
            max(false, (vars, cons) -> {
                if (vars.size == 1) {
                    cons.get("$L", Seq.with(vars.first()));
                    return;
                }

                StringBuilder format = new StringBuilder();
                Seq<Object> args = new Seq<>();

                ClassName cname = BaseProcessor.cName(Math.class);
                for (int i = 0; i < vars.size; i++) {
                    if (i == 0) {
                        format.append("$T.max($L, $L)");
                        args.add(cname, vars.get(0), vars.get(1));

                        i++;
                    } else {
                        format.insert(0, "$T.max($L, ").append(")");

                        args.insert(0, cname);
                        args.insert(1, vars.get(i));
                    }
                }

                cons.get(format.toString(), args);
            }),
            min(false, (vars, cons) -> {
                if (vars.size == 1) {
                    cons.get("$L", Seq.with(vars.first()));
                    return;
                }

                StringBuilder format = new StringBuilder();
                Seq<Object> args = new Seq<>();

                ClassName cname = BaseProcessor.cName(Math.class);
                for (int i = 0; i < vars.size; i++) {
                    if (i == 0) {
                        format.append("$T.min($L, $L)");
                        args.add(cname, vars.get(0), vars.get(1));

                        i++;
                    } else {
                        format.insert(0, "$T.min($L, ").append(")");

                        args.insert(0, cname);
                        args.insert(1, vars.get(i));
                    }
                }

                cons.get(format.toString(), args);
            }),

            // Boolean values.
            and(true, (vars, cons) -> {
                if (vars.size == 1) {
                    cons.get("$L", Seq.with(vars.first()));
                    return;
                }

                StringBuilder format = new StringBuilder();
                format.append((format.isEmpty() ? "$L" : " && $L").repeat(Math.max(0, vars.size)));

                cons.get(format.toString(), vars);
            }),
            or(true, (vars, cons) -> {
                if (vars.size == 1) {
                    cons.get("$L", Seq.with(vars.first()));
                    return;
                }

                StringBuilder format = new StringBuilder();
                format.append((format.isEmpty() ? "$L" : " || $L").repeat(Math.max(0, vars.size)));

                cons.get(format.toString(), vars);
            });

            public final boolean bool;
            public final Cons2<Seq<String>, Cons2<String, Seq<?>>> compute;

            Method(boolean bool, Cons2<Seq<String>, Cons2<String, Seq<?>>> compute) {
                this.bool = bool;
                this.compute = compute;
            }
        }
    }

    /** Loads texture regions but does not assign them to their acquirers */
    @Retention(RetentionPolicy.SOURCE)
    public @interface Load {
        /** @return The regions' name */
        String[] value();

        /** @return Whether it should outline the region, as a separate texture */
        boolean outline() default false;

        /** @return The outline color, only valid if {@link #outline()} is true */
        String outlineColor() default "464649";

        /** @return The outline radius, only valid if {@link #outline()} is true */
        int outlineRadius() default 4;
    }
}
