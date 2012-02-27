package cz.payola.web.client

import s2js.adapters.js.browser._
import cz.payola.web.shared.RpcTestRemote
import cz.payola.common.rdf.{ListItem, Graph}

object RpcTestClient
{
    def test() {
        window.alert(RpcTestRemote.foo(123, "xyz"))
    }

    def getGraph: Graph = {
        RpcTestRemote.getRandomGraph
    }

    def getList: ListItem = {
        RpcTestRemote.getRandomList
    }
}