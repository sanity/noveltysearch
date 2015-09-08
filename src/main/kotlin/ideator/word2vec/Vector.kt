package ideator.word2vec

data class Vector(val array: DoubleArray) {
    fun size() : Int {
        return array.size()
    }

    fun get(i : Int) : Double {
        return array[i]
    }

    fun apply(applyTo: Vector, toApply: (Double, Double) -> Double): Vector {
        assert(this.size() == applyTo.size())
        val destination = DoubleArray(this.size())
        for (i in 0..(this.size()-1)) {
            destination[i] = toApply(this[i], applyTo[i])
        }
        return Vector(destination)
    }

    fun div(divideBy : Double) : Vector {
        val arr = DoubleArray(size())
        for (ix in array.indices) {
            arr[ix] = array[ix] / divideBy
        }
        return Vector(arr)
    }

    fun plus(applyTo: Vector): Vector {
        return apply(applyTo) { a, b -> a + b }
    }

    fun minus(applyTo: Vector): Vector {
        return apply(applyTo) { a, b -> a - b }
    }

    fun distance(other: Vector): Double {
        val difference = this - other
        return Math.sqrt(difference.array.map({ v -> v * v }).sum())
    }

    fun magnitude() : Double {
        return Math.sqrt(array.map({ v -> v * v }).sum())
    }
}