package kg

import scala.collection.mutable.HashMap
    
class Node extends Element{
    def copy() : Node =
    {
        val copyNode = new Node()
        copyNode.setProperties(properties)
        return copyNode
    }
    
    override def toString() = this.properties.toString()

} // Node

object NodeTester extends App{

    println("Defining Road element...")

    class Road extends Node{}
    object Road
        extends NodeAlgebraic
        with NodeType("Road")

    Road.addProperty("name",Int)
    Road.addProperty("dir",Int)
    Road.addProperty("lanes",Int)
    Road.addProperty("type",Int)

    println("Creating roads...")

    try{
        val r1 = Road(HashMap[String,Primitive](
            "name"->1.0,"type"->4,"lanes"->1,"dir"->1))
    }catch{
        case e : Throwable => println("Didn't allow bad type. Test passed.")
    }
    val r2 = Road(HashMap[String,Primitive](
        "name"->2,"type"->3,"lanes"->2,"dir"->1))
    val r3 = Road(HashMap[String,Primitive](
        "name"->3,"type"->2,"lanes"->3,"dir"->1))
    val r4 = Road(HashMap[String,Primitive](
        "name"->3,"type"->1,"lanes"->4,"dir"->1))

    println("Defining Road2 element...")

    class Road2 extends Node{}
    object Road2
        extends NodeAlgebraic
        with NodeType("Road2")
    
    Road2.addProperty("name",Int)
    Road2.addProperty("dir",Int)
    Road2.addProperty("lanes",Int)
    Road2.addProperty("type",Int)

    println("Creating road2s...")
    val r5 = Road2(HashMap[String,Primitive](
        "name"->1,"type"->4,"lanes"->1,"dir"->1))
    val r6 = Road2(HashMap[String,Primitive](
        "name"->3,"type"->2,"lanes"->3,"dir"->1))
    val r7 = Road2(HashMap[String,Primitive](
        "name"->5,"type"->2,"lanes"->3,"dir"->1))

    println("Defining Sensor element")
    class Sensor extends Node{}
    object Sensor
        extends NodeAlgebraic
        with NodeType("Sensor")
  
    Sensor.addProperty("id",Int)
    Sensor.addProperty("district",Int)

    println("Creating sensors...")
    val s1 = Sensor(HashMap[String,Primitive]("id"->1,"district"->4))
    val s2 = Sensor(HashMap[String,Primitive]("id"->2,"district"->3))
    val s3 = Sensor(HashMap[String,Primitive]("id"->3,"district"->2))
    val s4 = Sensor(HashMap[String,Primitive]("id"->4,"district"->1))

    println("Testing NodeAlgebraic operations...")
    
    println(s"Road: \n${Road}")
    println()
    println(s"Sensor: \n${Sensor}")
    println()
    println(s"Road2: \n${Road2}")
    println()
    
    val Sel = Road.select("name", _==3)
    println(s"Road.select(name, _==3): \n${Sel}")
    println()
    
    val Proj = Road.project("name","type")
    println(s"Road.project(name,type): \n${Proj}")
    println()
    
    val Union = Road.union(Sensor)
    println(s"Road.union(Sensor): \n${Union}")
    println()
    
    val Inter = Road.intersect(Road2) 
    println(s"Road.intersect(Road2): \n${Inter}")
    println()

    println(s"Road: \n${Road}")
    println(s"Road2: \n${Road2}")
    val Diff = Road.minus(Road2)
    println(s"Road.minus(Road2): \n${Diff}")
    
} // NodeTester