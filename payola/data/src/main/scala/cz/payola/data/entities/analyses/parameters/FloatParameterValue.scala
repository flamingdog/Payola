package cz.payola.data.entities.analyses.parameters

import cz.payola.data.entities.analyses.ParameterValue
import cz.payola.data.PayolaDB

object FloatParameterValue {

    def apply(p: cz.payola.domain.entities.analyses.parameters.FloatParameterValue): FloatParameterValue = {
        p match {
            case param: FloatParameterValue => param
            case _ => {
                val parameter = FloatParameter(p.parameter.asInstanceOf[cz.payola.domain.entities.analyses.parameters.FloatParameter])
                val parameterValue = new FloatParameterValue(p.id, parameter, p.value)

                parameter.registerParameterValue(parameterValue)

                parameterValue
            }
        }
    }
}

class FloatParameterValue(
    override val id: String,
    param: FloatParameter,
    override var value: Float)
    extends cz.payola.domain.entities.analyses.parameters.FloatParameterValue(param, value)
    with ParameterValue[Float]
{
    val parameterId: Option[String] = if (param == null) None else Some(param.id)

    private lazy val _parameterQuery = PayolaDB.valuesOfFloatParameters.right(this)

    override def parameter: ParameterType = evaluateCollection(_parameterQuery)(0)
}
