package kg

class InvalidPropertyException(message : String) extends Exception(message)
    
import scala.collection.mutable.HashMap    
import scala.math.max
trait ElementType(_name : String) {

    var numElements = 0 
    
    var elements = Vector[Element]()

    val schema = HashMap[String,PrimitiveType]("id"->Int)
 
    def apply(properties : Tuple2[String,Primitive]*) : Element =
    {
        val map = HashMap[String,Primitive]()
        for (property <- properties) map.update(property._1,property._2)
        val element = new Element(numElements)
        element.setProperties(map)
        elements = elements :+ element
        numElements = numElements+1
        element        
    }

    def apply(properties : HashMap[String,Primitive]) : Element =
    {
	val validationResult = validatePropertyDomain(properties)
	val element = new Element(numElements)
        if(validationResult==true)
	{
		element.setProperties(properties)
        	elements = elements :+ element
        	numElements = numElements+1
	}
        element
    }


    def validatePropertyDomain(properties : HashMap[String,Primitive]) : Boolean =
    {
    val s = schema
    var result = false
    var falseCount = 0				
	if(s.size-1 == properties.size){
			for (property <- properties) 
			{

				val expected = (s(property._1).getClass).
						toString.
                          			replace("$","").
                          			replace("class scala.","")

										
        			val passed = ((property._2).getClass).
						toString.
						replace("class java.lang.","").
                          			replace("Integer","Int").replace("String","class scalation.math.StrO")        			

				if ( expected != passed )
				{
					println(s"Couldn't insert value ${properties}")
					falseCount= falseCount+1
				     	println(s"""Value ${property._2} for ${property._1} has invalid data type.
                                                   |Type ${expected} expected, type ${passed} passed.\n""".stripMargin)
			    	}
			
			}
			if(falseCount==0) result=true
			result
	}
	else 
	{
		println(s"Required: ${s} size: ${s.size}\n  Found: ${properties} size:${properties.size}")    
    		result
	}	
    
    }
    
    def name = _name
    
    def getElements() : Vector[Element] = elements ++ Vector[Element]()
    
    def add(_elements : Element *) =
    {
        for (e <- _elements if (!elements.contains(e))) elements = elements :+ e
    }

    def add(_elements : Vector[Element]) =
    {
        for (e <- _elements if (!elements.contains(e))) elements = elements :+ e
    }
    
    def addSchema( pAndDs : Tuple2[String,PrimitiveType] *) =
    {
        for (pAndD <- pAndDs) schema.update(pAndD._1,pAndD._2)
    }

    def addSchema( schema : HashMap[String,PrimitiveType]) =
    {
        this.schema.addAll(schema)
    }

    def addProperty( property : Tuple2[String,PrimitiveType]) =
    {
        this.schema. += ((property._1,property._2))
    }
    
    def getSchema() = schema.clone()

    def contains(element : Element) = elements.contains(element)

    def empty() : ElementType =
    {
        object EmptyThing extends ElementType(_name)
        EmptyThing.addSchema(schema)
        return EmptyThing
        
    }
    def select  ( property  : String, 
                  p         : (Primitive) => Boolean) : ElementType =
    {
        
     
   object SelectThing
            extends ElementType(_name)

        //for (pd <- schema) SelectThing.addProperty(pd)
        SelectThing.addSchema(schema)
        schema.get(property) match{
            case Some(i)    =>
                {            
                    for (element <- elements) {
                        try{
                            if (p(element(property))) SelectThing.add(element)
                        }
                        catch{
                            case _ => 
                        }
                    }
                }
            case None       =>
                {
                    println(s"Invalid select property")
                    println(s"${property} not in schema for ${_name}")
                }
        } // match

	println("s ${SelectThing}In select thing")
        SelectThing
    }


    
     def selectE  ( property  : String,
                  p         : (Primitive) => Boolean) : Vector[Element] =
    {

	//class SelectThing extends Element(_name)
	object SelectThing extends ElementType(_name)
	var result = Vector[Element]()

        //for (pd <- schema) SelectThing.addProperty(pd)

        SelectThing.addSchema(schema)
        schema.get(property) match{
            case Some(i)    =>
                {
                    for (element <- elements) {
                        try{
                       
		            if (p(element(property))) result = result :+ element
                       
		        }
                        catch{
                            case _ =>
                        }
                    }
                }
            case None       =>
                {
                    println(s"Invalid select property")
                    println(s"${property} not in schema for ${_name}")
                }
        } // match
       
        result

    }





    def selectOR  ( criteria : Tuple2[String,Primitive=>Boolean] *) : ElementType =
    {
        object SelectThing
            extends ElementType(_name)

        SelectThing.addSchema(schema)

        for (element <- elements){
            val add = criteria.foldLeft(false)((x,criterion) =>
            {
                val (property,predicate) = criterion
                try{
                    x || predicate(element(property))
                }catch{
                    case _ => x || false
                }
            })
            if (add) SelectThing.add(element)
        }
    
/*	for (property <- properties){
        	schema.get(property._1) match{
        	    case Some(i)    =>
        	        {            
        	            for (element <- elements) {
        	                try{
        	                    if (property._2(element(property._1))) SelectThing.add(element)
        	                }
        	                catch{
        	                    case _ => 
        	                }
        	            }
        	        }
        	    case None       =>
        	        {
        	            println(s"Invalid select property")
        	            println(s"${property} not in schema for ${_name}")
        	        }
        	} // match
        }
*/  
        SelectThing
    }
    
    def selectAND  ( criteria : Tuple2[String,Primitive=>Boolean] *) : ElementType =
    {
        object SelectThing
            extends ElementType(_name)

        SelectThing.addSchema(schema)
    
        for (element <- elements) {
            val add = criteria.foldLeft(true)((x,criterion) => {
                val (property,pred) = criterion
                try{
                    x && pred(element(property))
                }catch{
                    case _ => x && false
                }
            })
            if (add) SelectThing.add(element)
        }
    
        SelectThing
    }
        
    /*
     *  Return a projection of this ElementType projected
     *  onto a subset of its properties.
     */
    def project(properties : String*) : ElementType =
    {
        object ProjectThing extends ElementType(_name)

        ProjectThing.addSchema(schema.filter( (k,v) => {
            properties.contains(k)
        }))

        elements.foreach( e => {
            ProjectThing( e.getProperties().filter( (k,v) => {
                properties.contains(k)
            }))
        })

        ProjectThing
    }

    def union ( other : ElementType ) : ElementType =
    {
        
        object UnionThing extends ElementType(_name)
        UnionThing.addSchema(schema)
    
        if (schema == other.getSchema()){
            elements.foreach(element => UnionThing.add(element)) 
            other.getElements().foreach( element => {
                if (!UnionThing.contains(element)) UnionThing.add(element)
            })
        }
        else{
            println("Error in ElementType union.")
            println(s"This:\n${schema}")
            println(s"Not unnion compatible with other:\n${other.getSchema()}")       
        }
        return UnionThing
    }

    def OR (other : ElementType) = this union other
    
    def intersect(other : ElementType) : ElementType =
    {
        object IntersectThing extends ElementType(_name)
        IntersectThing.addSchema(schema)
        if( schema == other.getSchema() ){
            elements.filter( element =>
                other.contains(element)
            ).foreach( element => 
                IntersectThing.add(element)  
            )
        } // if
        else{ 
            println(s"Incompatible ElementTypesfor intersect.")
            println(s"this: ${schema}")
            println(s"other: ${other.getSchema()}")
        }
        return IntersectThing
    }

    def AND(other : ElementType) = this intersect other
    
    def minus(other : ElementType) : ElementType =
    {
        object MinusThing extends ElementType(_name)
        MinusThing.addSchema(schema)
        if( schema == other.getSchema() ){
            elements.filterNot( element =>
                other.contains(element)
            ).foreach( element => 
                MinusThing.add(element)  
            )
        } // if
        else{ 
            println(s"Incompatible ElementTypes for Intersect.")
            println(s"this: ${schema}")
            println(s"other: ${other.getSchema()}")
        }
        return MinusThing
        
    }

    def BUT_NOT(other : ElementType) = this minus other
    
    def prettyPrintElements   ( name : String,
                                elements : Vector[HashMap[String,Primitive]],
                                properties : String*
                              ) : String =
    {
        val maxLength = properties.map(_.length).max

        val width =  properties.size * (maxLength +1) + 1
        val sep = "-" * (maxLength) + "+"
        val ssep = "+" + sep * properties.size
        val topBorder = ssep
        val botBorder = topBorder 
        
        var str = s"${name}:\n" + topBorder + "\n+"

        var args = List[Any]()
    
        for (property <- properties) {
            str = str + "%" + maxLength + "s+"
            args = args :+ property
        }

        str = str + "\n" + topBorder + "\n"
        for (element <- elements) {
            str = str + "+"
            for ( property <- properties ) {
                element.get(property) match{
                    case None       => {
                        str = str + "%"+maxLength+"s+"
                        args = args :+ ""
                    } // case None
                    case Some(i)    => {
                        args = args :+ i
                        schema.get(property) match{
                            //TODO MATCH THE CASES CORRECTLY
                            case _ => str = str + "%" + maxLength + "d+"
                        } // match
                    } // case Some(i)
                } // match
            } // for
            str += "\n" + ssep + "\n"
        } // for
        str.format(args:_*)
    }

    override def toString() =
    {
        val _schema = schema.toList
        val maxFieldWidths = Array.ofDim[Int](_schema.length)
        var maxValueLength = 0
        for ( property <- 0 until _schema.length ) {
            maxValueLength = {
            if (elements.length > 0) 
                elements.map( element => 
                    s"${element.getOrElse(_schema(property)._1,"")}".length
                ).max
            else 0
            }
            maxFieldWidths(property) = max(name.length,max(maxValueLength + 2,_schema(property)._1.length+2))
        }
        var str = "+"
        var border = "+"
        var value : Primitive = 0
        var lPad = 0
        var rPad = 0
        var pad = 0
        for ( i <- 0 until _schema.length){
            border = border + "-" * maxFieldWidths(i) + "+"
            pad = maxFieldWidths(i) - _schema(i)._1.length
            lPad = pad/2
            rPad = pad - lPad          
            str = str                       +
                  " " * lPad                +
                  _schema(i)._1.toUpperCase +   
                  " " * rPad                +
                  "+"
        }
        str = str + "\n" + border + "\n"
        for ( element <- elements) {
            for ( i <- 0 until _schema.length) {
                value = element.getOrElse(_schema(i)._1,"")
                pad = maxFieldWidths(i) - s"${value}".length
                lPad = pad/2
                rPad = pad-lPad
                str = str + s"+${" " * lPad}${value}${" " * rPad}"
            }
            str += "+\n"
            str += border + "\n"
        }
        pad = border.length - name.length - 2
        lPad = pad/2
        rPad = pad-lPad 
        return  s"+${"-" * (border.length-2) }+"               + "\n" +
                s"+${" "*lPad}${name.toUpperCase}${" "*rPad}+" + "\n" +
                border                                         + "\n" + 
                str 
    } // toString

    def +(other : ElementType) = this union other
    def *(other : ElementType) = this intersect other
    def -(other : ElementType) = this minus other
}

class NodeType(name : String) extends ElementType(name){}
class EdgeType(name : String) extends ElementType(name){}

/*
 *  Tests the basic functionality of the NodeType and EdgeType classes:
 *      addSchema,
 *      add,
 *      apply, and
 *      print,
 *      select,
 *      project,
 *      union,
 *      intersect,
 *      minus
 *  Doesn't test schema restrictions...    
 */  
/*object ElementTypeTesterOne extends App{

    class Road(id : Int) extends Node(id){}
    object Road extends NodeType("Road"){}
    
    Road.addSchema(
        ("name",Int),
        ("type",Int),
        ("lanes",Int),
        ("dir",Int)
    )

    val r1 = Road(("name",2),("type",3),("lanes",2),("dir",1))
    val r2 = Road(("name",3),("type",2),("lanes",3),("dir",2))
    val r3 = Road(("name",4),("type",1),("lanes",4),("dir",1))
    val r4 = Road(("name",5),("type",1),("lanes",4),("dir",4))

    println(s"Road:\n${Road}")
    
    class Road2(id : Int) extends Node(id){}
    object Road2 extends NodeType("Road2"){}

    Road2.addSchema(Road.getSchema())

    val r5 = Road2(("name",2),("type",3),("lanes",2),("dir",1))
    
    println(s"Road2 before:\n${Road2}")
    Road2.add(Road.getElements())
    println(s"Road2 after:\n${Road2}")

    println("Selecting...")
    val Sel1 = Road.select("lanes",_==4)
    val Sel2 = Road2.select("dir",_==1)
    println(s"Sel1:\n${Sel1}")
    println(s"Sel2:\n${Sel2}")

    val Sel6 = Road.selectOR(   ("lanes",_==4),
                                ("dir",_==1) )
    print(s"Sel6 : \n${Sel6}")
    val Sel7 = Road.selectAND(  ("dir",_==1),
                                ("type",_==3))
    print(s"Sel7 : \n${Sel7}")
    
    println("Projecting...")
    val Proj1 = Road.project("lanes","dir")
    val Proj2 = Sel1.project("type")
    val Proj3 = Road.project("invalid")
    println(s"Proj1:\n${Proj1}")
    println(s"Proj2:\n${Proj2}")
    println(s"Proj3:\n${Proj3}")

    println("Unioning...")
    val Sel3 = Road.select("lanes",_==4)
    val Sel4 = Road.select("lanes",_==2)
    val Sel5 = Road.select("lanes",_==3)
    val Uni1 = Sel3 union Sel4  union Sel5
    val Uni2 = Sel3 + Sel4 + Sel5
    println(s"Uni1: \n${Uni1}")
    println(s"Uni2: \n${Uni2}")
    println(s"Uni3: \n${Sel3 OR Sel4 OR Sel5}")
    
    println("Intersecting...")
    val Int1 = Sel1.intersect(Sel2)
    println(s"Int1:\n${Int1}")
    println(s"Int1:\n${Sel1 AND Sel2}")

    println("Minusing...")
    val Min1 = Road.minus(Sel1)
    println(s"Min1:\n${Min1}")
    println(s"Min1:\n${Road BUT_NOT Sel1}")
    val r6 = Road(("type",3),("lanes",2),("dir",1))
    val r7 = Road(("name",2),("lanes",2),("dir",1))

    println(s"r7: \n${r7}")
}

object ElementTypeTesterTwo extends App{

    class Road(id : Int) extends Node(id){}
    object Road extends NodeType("Road"){}
    
    Road.addSchema(
        ("name",Int),
        ("type",Int),
        ("lanes",Int),
        ("dir",Int)
    )

    val r1 = Road(("name",2),("type",3),("lanes",2),("dir",1))
    val r2 = Road(("name",3),("type",2),("lanes",3),("dir",2))
    val r3 = Road(("name",4),("type",1),("lanes",4),("dir",1))
    val r4 = Road(("name",5),("type",1),("lanes",4),("dir",4))

    println(s"Road:\n${Road}")
    
    for ( pv <- r1.iterator ) println(s"pv: ${pv}")
}
*/

object SampleTestHashMap extends App{

        class Freeway(id : Int) extends Node(id){}
        object Freeway extends NodeType("Freeways"){}
	val map = HashMap[String,PrimitiveType](
						("ref"->Int),
                				("city"->Int),
                				("district"->Int),
                				("name"->Int)
                				)
	
	Freeway.addSchema(
		map		
                )

        val US101S = Freeway(
				HashMap[String,Primitive](
							("ref"->"US 101-S"),
							("city"->"SanFrancisco"),
							("district"->4),
							("name"->"James Lick Freeway")
							)
				)

  	
        val US101N = Freeway(
                                HashMap[String,Primitive](
                                                        ("ref"->"US 101-N"),
                                                        ("city"->"SanFrancisco"),
                                                        ("district"->4),
                                                        ("name"->"James Lick Freeway")
                                                        )
                                )





	
	val CentralFreeway = Freeway(
	    		       HashMap[String,Primitive](
                                                        ("ref"->"US 101"),
                                                        ("city"->"SanFrancisco"),
                                                        ("district"->4),
                                                        ("name"->"Central Freeway")
                                                        )
					)                            


	println(s"Freeways:\n${Freeway}")       


              
        class Station(id : Int) extends Node(id){}
        object Station extends NodeType("Station"){}

        Station.addSchema(
		HashMap[String,PrimitiveType]( 
		                ("StationName"->Int),
                		("FreewayID"->Int),
                		("StationId"->Int),
                		("Lat"->Int),
                		("Long"->Int),
                		("Abs PM"->Int),
                		("Type"->Int)
                		)
			)

        val BlankenAve_S = Station( HashMap[String,Primitive]( 
	    		   	    			 ("StationName"->"Blanken Ave"),
							 ("FreewayID"->"US101-S"),
							 ("StationId"->404569),
							 ("Lat"->37.710838),
							 ("Long"-> -122.395652),
							 ("Abs PM"->428.50),
							 ("Type"->"Mainline"))
						)        

	val ValenciaSt_S = Station( HashMap[String,Primitive](
							("StationName"->"Valencia St"),
							("FreewayID"->"US101-S"),
							("StationId"->401820),
							("Lat"->37.769613),
							("Long"-> -122.416876),
							("Abs PM"->434.18),
							("Type"->"Mainline"))
      				    		)

        val VermontSt_S = Station( HashMap[String,Primitive]( 
	    		  	   			("StationName"->"Vermont St"),
							("FreewayID"->"US101-S"),
							("StationId"->401410),
							("Lat"->37.75671),
							("Long"-> -122.403619),
							("Abs PM"->431.86),
							("Type"->"Mainline"))
						)

        val BlankenAve_N = Station( HashMap[String,Primitive]( 
							("StationName"->"Blanken Ave"),
							("FreewayID"->"US101-N"),
							("StationId"->404528),
							("Lat"->37.710864),
							("Long"-> -122.395412),
							("Abs PM"->428.46),
							("Type"->"Mainline"))
        					)
	
	val ValenciaSt_N = Station( HashMap[String,Primitive]( 
							("StationName"->"Valencia St"),
							("FreewayID"->"US101-N"),
							("StationId"->401819),
							("Lat"->37.77007),
							("Long"-> -122.419351),
							("Abs PM"->433.47),
							("Type"->"Mainline"))
							)        

	val VermontSt_N = Station( HashMap[String,Primitive](
							("StationName"->"Vermont St"),
							("FreewayID"->"US101-N"),
							("StationId"->401409),
							("Lat"->37.756863),
							("Long"-> -122.403503),
							("Abs PM"->431.82),
							("Type"->"Mainline"))
							)

	val Test_Station = Station(("StationName","noname"))

        
	println(s"Stations:\n${Station}")

        class Road(id : Int) extends Node(id){}
        object Road extends NodeType("Road"){}

	Road.addSchema( HashMap[String,PrimitiveType]( 
                				   ("name"->Int),
		        			   ("type"->Int),
						   ("Roadid"->Int)
                				   )
				)


	val BlankenAvenue = Road( HashMap[String,Primitive]( 
	    		    	  		("name"->"Blanken Ave"),
			  			("type"->"tertiary"),
			  			("Roadid"->255178047))
			  	)	

	val VermontStreet = Road( HashMap[String,Primitive]( 
	    		    	  		("name"->"Vermont St"),
						("type"->"secondary"),
						("Roadid"->254759963))
			  	)

        val ValenciaStreet = Road( HashMap[String,Primitive]( 
	    		     	   		("name"->"Valencia St"),
						("type"->"tertiary"),
						("Roadid"->3999))
				)

	val SeventeenStreet = Road( HashMap[String,Primitive]( 
	    		      	    		("name"->"17th St"),
						("type"->"residential"),
						("Roadid"->27028807))
       				)
 
	println(s"Roads:\n${Road}")

	class Way(id : Int) extends Node(id){}
	object Way extends NodeType("Ways"){}

	Way.addSchema( HashMap[String,PrimitiveType]( 
		       				  ("Wayid"->Int),
                				  ("type"->Int),
		        			  ("lat"->Int),
                				  ("long"->Int))
				)

        val Way1 = Way(HashMap[String,Primitive]( 
	    	   				  ("Wayid"->"3999500657"),
						  ("type"->"node"),
						  ("lat"->37.7653318),
						  ("long"-> -122.4045732))
				)

	val Way2 = Way(HashMap[String,Primitive]( 
	    	   				  ("Wayid"->"65317350"),
						  ("type"->"node"),
						  ("lat"->37.7646654),
						  ("long"-> -122.4045092))
				)

	val Way3 = Way(HashMap[String,Primitive]( 
						  ("Wayid"->"4179475357"),
						  ("type"->"node"),
						  ("lat"->37.7555649),
						  ("long"-> -122.4209898))
				)

	val Way4 = Way(HashMap[String,Primitive]( 
						  ("Wayid"->"65319643"),
						  ("type"->"traffic_signals"),
						  ("lat"->37.7553033),
						  ("long"-> -122.4209648))
				 )

	val Way5 = Way(HashMap[String,Primitive]( 
						  ("Wayid"->"65317347"),
						  ("type"->"node"),
						  ("lat"->37.764723),
						  ("long"-> -122.403527))
				)

	val Way6 = Way(HashMap[String,Primitive]( 
	    	   				  ("Wayid"->"65317350"),
						  ("type"->"node"),
						  ("lat"->37.7646654),
						  ("long"-> -122.4045092))
				)

	val Way7 = Way(HashMap[String,Primitive]( 
						  ("Wayid"->"65371602"),
						  ("type"->"node"),
						  ("lat"->37.749828),
						  ("long"-> -122.4036508))
				)

	val Way8 = Way(HashMap[String,Primitive]( 
						  ("Wayid"->"65371603"),
						  ("type"->"node"),
						  ("lat"->37.7482208),
						  ("long"-> -122.4041557))
				)

	println(s"Ways:\n${Way}")

	class Has(id : Int) extends Edge(id){}
	object Has extends EdgeType("Has"){}

        class Intersects(id : Int) extends Edge(id){}
        object Intersects extends EdgeType("Intersects"){}

	val rel_1    = Relation(US101S,Has(),BlankenAve_S)
        val rel_2    = Relation(US101S,Has(),ValenciaSt_S)
        val rel_3    = Relation(US101S,Has(),VermontSt_S)

	val rel_4    = Relation(US101N,Has(),BlankenAve_N)
        val rel_5    = Relation(US101N,Has(),ValenciaSt_N)
	val rel_6    = Relation(US101N,Has(),VermontSt_N)


        val rel_7    = Relation(VermontStreet,Has(),Way1)
        val rel_8    = Relation(VermontStreet,Has(),Way2)

        val rel_9    = Relation(ValenciaStreet,Has(),Way3)
        val rel_10    = Relation(ValenciaStreet,Has(),Way4)

	val rel_11    = Relation(SeventeenStreet,Has(),Way5)
	val rel_12    = Relation(SeventeenStreet,Has(),Way6)

        val rel_13    = Relation(SeventeenStreet,Intersects(),VermontStreet)

	val rel_14    = Relation(US101S,Has(),Way7)
	val rel_15    = Relation(US101S,Has(),Way8)

	val rel_16    = Relation(CentralFreeway,Has(),Test_Station)


	val MapDB = new Graph()
	MapDB.updateSchema(
	        (Freeway,Has,Station),
		(Freeway,Has,Way),
		(Road,Has,Way),
		(Road,Intersects,Road),
		(Freeway,Intersects,Freeway)
		)
	MapDB.addNodeTypes(Freeway,Road,Way,Station)
	MapDB.addEdgeTypes(Has,Intersects)
	MapDB.addRelations(rel_1,
			rel_2,
                        rel_3,
			rel_4,
			rel_5,
                        rel_6,
			rel_7,
                        rel_8,
                        rel_9,
			rel_10,
                        rel_11,
                        rel_12,
			rel_13,
                        rel_14,
			rel_15,
                        rel_16
                        )

	println(s"\n***************the MapDB graph database, as a reference*********************\n")
        println(s"\nMapDB: \n${MapDB}")

        println(s"\n************getting the paths of Freeway James Lick Freeway  ******************\n")
	println(s"${MapDB.paths(Freeway.select("name",_=="James Lick Freeway"),Has,Station)}")
	println(s"\n***** Freeway with station name Blanken Ave **************\n")
        println(s"${MapDB.paths(  Freeway                                     ,
                                Has                                        ,
				Station.select( "StationName",_=="Blanken Ave")                                                         )}")

	println(s"\n***** Gets path if freeay(US101-S) has Station or 17th Street intersects with any other Road  ******\n")
	println(s"${MapDB.paths(  (Freeway.select("name",_=="Central Freeway"),Has ,Station),
       	(Road.select("name",_=="17th St"   ),Intersects,Road)    )}")


	
}


object OSM_Test extends App{

import scalation.math.{Complex, Rational, Real}
import scalation.math.StrO
import scala.io.Source
import scala.util.parsing.json.JSON._
		
	/*
         *  Creating Required Classes and Campanion Objects for Nodes and Edges
         */  

        class Nodes(id : Int) extends Node(id){}		//for Nodes
        object Nodes extends NodeType("Nodes"){}
        Nodes.addSchema( HashMap[String,PrimitiveType](
                                        ("lat"->Double),
                                        ("nodeId"->Double),
                                        ("long"->Double),
					("highway"->StrO))

                        )	

	class Way(id : Int) extends Node(id){}			//for Ways
        object Way extends NodeType("Ways"){}
        Way.addSchema( HashMap[String,PrimitiveType](
                                       ("city"->StrO),
                                       ("wayId"->Double),
                                       ("name"->StrO),
                                       ("highway"->StrO),
                                       ("nameBase"->StrO),
                                       ("nameType"->StrO),
				       ("cfcc"->StrO))
                     )
		     
	class Road(id : Int) extends Node(id){}                  //for Roads
        object Road extends NodeType("Roads"){}
        Road.addSchema( HashMap[String,PrimitiveType](
                                       ("name"->StrO))
                     )
	var roadNames = Vector[String]()

	class Has(id : Int) extends Edge(id){}			// for Has Edge
        object Has extends EdgeType("Has"){}
        var Has_Relations = Vector[Relation]() 

	class Intersects(id : Int) extends Edge(id){}		// for Intersects Edge
        object Intersects extends EdgeType("Intersects"){}
        Intersects.addProperty(("IntersectingNode",Double))
        var Intersects_Relations = Vector[Relation]()   
	

	 /*
         *  Getting JSON Data for Nodes and Ways
         */

         var json:Option[Any] = parseFull(Source.fromFile("jsons/test1.json").mkString)
         var map:Map[String,Any] = json.get.asInstanceOf[Map[String, Any]]
         val elements:List[Any] = map.get("elements").get.asInstanceOf[List[Any]]  
         
         elements.foreach( langMap => {

	 val emap:Map[String,Any] = langMap.asInstanceOf[Map[String,Any]]

	 val id:Double = emap.get("id").get.asInstanceOf[Double]
         val e_type:String= emap.get("type").get.asInstanceOf[String]
           
	 /*
         *  Create Node instances for nodes, ways and Has relation
         */  
         if (e_type == "node"){

            	    val lat:Double = emap.get("lat").get.asInstanceOf[Double]
          	    val lon:Double = emap.get("lon").get.asInstanceOf[Double]
	  	    val tags:Map[String,Any] = emap.get("tags").getOrElse(Map[String, String]()).asInstanceOf[Map[String, Any]]
		    val highway:String = tags.get("highway").getOrElse("None").asInstanceOf[String]
		  
		    val nodeProperties = HashMap[String,Primitive](
					("nodeId"->id),
                                	("lat"->lat),
                                	("long"->lon),
					("highway"->highway))
		    
		    var node= Nodes(nodeProperties)        // Creating Instance for every Node

	 	    }
	 
	 if (e_type == "way"){

                     val nodesList:List[Double] = emap.get("nodes").get.asInstanceOf[List[Double]]
                     val tags:Map[String,Any] = emap.get("tags").getOrElse(Map[String, String]()).asInstanceOf[Map[String, Any]]
		     val highway:String= tags.get("highway").getOrElse("None").asInstanceOf[String]                     
		     val city:String= tags.get("tiger:county").getOrElse("None").asInstanceOf[String]
                     val name:String= tags.get("name").getOrElse("None").asInstanceOf[String]
		     val nameBase:String= tags.get("tiger:name_base").getOrElse("None").asInstanceOf[String]
		     val nameType:String= tags.get("tiger:name_type").getOrElse("None").asInstanceOf[String]
		     val cfcc:String= tags.get("tiger:cfcc").getOrElse("None").asInstanceOf[String]

		     val nodes_List:List[Double] = ( nodesList )
		     
		     roadNames = roadNames :+ name
                     val wayProperties = HashMap[String,Primitive](
                                                  ("city"->city),
                                                  ("name"->name),
                                                  ("wayId"->id),
						  ("highway"->highway),
						  ("nameBase"->nameBase),
						  ("nameType"->nameType),
						  ("cfcc"->cfcc)  
						  )
				
                     var way= Way(wayProperties)

		     for(j<-0 to (nodes_List.size)-1)
                     {
			val selected_node= Nodes.selectE("nodeId",_== nodes_List(j))    //Returns Vector[Element] with only one value because every node has different node_id
                	if(selected_node.size>0)
				{
                        	val rel = Relation(way,Has(),selected_node(0))
                        	Has_Relations = Has_Relations :+ rel
                        	}
                	}
		 
                }
        })

	println("*Created Node,Ways and Has Relation*")
	
	roadNames = roadNames.distinct
	for(i<- 0 to roadNames.size-1)
	{
	var road = Road(HashMap[String,Primitive]("name"->roadNames(i))
	    	   )
	}



	 /*
         *  Reading intersectionsTest file and creating Intersections Relations
         */
	
	var json2:Option[Any] = parseFull(Source.fromFile("jsons/intersectionsTest.json").mkString)
        var map2:Map[String,Double] = json2.get.asInstanceOf[Map[String,Double]]

	println("**Creating Intersects Relations**")
        map2.foreach( eleMap => {
		      var way= (eleMap._1).split(",")
		      var way1_id = (way(0)).toDouble
		      var way2_id = (way(1)).toDouble
		      		      
		      val intersectingNode = eleMap._2
		      
		      val WAY_1 = Way.selectE("wayId",_== way1_id)
		      val WAY_2 = Way.selectE("wayId",_== way2_id)
		      		      
		      if(WAY_1.size>0 && WAY_2.size>0 )
		      {
		      val rel = Relation(WAY_1(0),Intersects(("IntersectingNode",intersectingNode)),WAY_2(0))
		      Intersects_Relations = Intersects_Relations :+ rel
		      }		     

	})

	 /*
         *  Storing Values in Graph
         */
	
	val OSM_DB = new Graph()
	
	OSM_DB.updateSchema(
		(Way,Has,Nodes),
		(Way,Intersects,Way)
	)	
	
	OSM_DB.addNodeTypes(Nodes,Way,Road)
	OSM_DB.addEdgeTypes(Has,Intersects)

	OSM_DB.addRelations(Has_Relations)
	OSM_DB.addRelations(Intersects_Relations)

	println(s"\n***************  MapDB graph database  *********************\n")
        println(s"\nMapDB: \n${OSM_DB}")

}