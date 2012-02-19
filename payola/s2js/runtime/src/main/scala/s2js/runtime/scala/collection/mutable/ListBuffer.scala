package s2js.runtime.scala.collection.mutable

import s2js.compiler.NativeJs

object ListBuffer extends s2js.runtime.scala.collection.SeqCompanion
{
    def empty = new ListBuffer

    @NativeJs("return self.fromJsArray(xs.internalJsArray);")
    def apply(xs: Any*): s2js.runtime.scala.collection.Seq = null
}

class ListBuffer extends s2js.runtime.scala.collection.Seq
{
    def newInstance = ListBuffer.empty
}