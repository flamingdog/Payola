package s2js.compiler.components

import collection.mutable
import tools.nsc.Global

/** A compiler of a ClassDef that corresponds to a class or trait. */
class ClassCompiler(packageDefCompiler: PackageDefCompiler, classDef: Global#ClassDef)
    extends ClassDefCompiler(packageDefCompiler, classDef)
{
    override val memberContainerName = fullJsName + ".prototype"

    protected def compileConstructor(parentConstructorCall: Option[Global#Apply]) {
        buffer += fullJsName + " = function(";
        compileParameterDeclaration(constructorParameters)
        buffer += ") {\n"
        buffer += "var self = this;\n"

        val initializedValDefs = new mutable.HashSet[String]
        if (constructorDefDef.isDefined) {
            compileParameterInitialization(constructorParameters)

            // Initialize fields specified as the implicit constructor parameters.
            constructorParameters.get.foreach {parameter =>
                initializedValDefs += packageDefCompiler.getSymbolLocalJsName(parameter.symbol)
                buffer += "self.%1$s = %1$s;\n".format(packageDefCompiler.getSymbolJsName(parameter.symbol))
            }
        }

        // Initialize fields that aren't implicit constructor parameters.
        valDefs.filter(v => !initializedValDefs.contains(packageDefCompiler.getSymbolLocalJsName(v.symbol))).foreach(
            compileMember(_, "self"))

        // Call the parent class constructor.
        if (parentClass.isDefined && parentConstructorCall.isDefined) {
            compileParentCall(parentConstructorCall.get.args)
            buffer += ";\n"
        }

        // Compile the constructor body.
        classDef.impl.body.filter(!_.isInstanceOf[Global#ValOrDefDef]).foreach {ast =>
            compileAst(ast)
            buffer += ";\n"
        }
        buffer += "};\n"

        if (parentClass.isDefined) {
            buffer += "goog.inherits(%s, %s);\n".format(
                fullJsName,
                packageDefCompiler.getSymbolJsName(parentClass.get.symbol)
            )
        }
    }

    override protected def compileMembers() {
        // The valDefs are compiled within the constructor.
        defDefs.foreach(compileMember(_))
    }
}