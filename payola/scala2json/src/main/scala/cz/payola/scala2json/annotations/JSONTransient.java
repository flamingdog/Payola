package cz.payola.scala2json.annotations;

import java.lang.annotation.*;

/**
 * Allows the field to be ignored by the JSON serialization process.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JSONTransient {

}
