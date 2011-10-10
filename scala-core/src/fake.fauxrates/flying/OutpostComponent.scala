package fake.fauxrates
package flying

import ES._

class OutpostComponent (var name : String, var x : Double, var y : Double) extends Component {

	/* a pretty little getsetter */
	def XY = (x,y)
	def XY_= (t : (Double,Double)) = (x,y) = t
}