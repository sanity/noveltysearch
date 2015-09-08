package ideator.pca

import Jama.Matrix
import com.mkobos.pca_transform.PCA

/**
 * Created by ian on 9/6/15.
 */

fun buildPCA(inputData : Array<DoubleArray>) : PCATransformer {
    val inputMatrix = Matrix(inputData)
    return PCATransformer(PCA(inputMatrix))
}

class PCATransformer(val pca : PCA) {
    fun transform(input : DoubleArray, maxOutputDimensions : Int = Integer.MAX_VALUE) : DoubleArray {
        val inputAsSingleton = Array(1, {ix  -> input})
        val outputMatrix = pca.transform(Matrix(inputAsSingleton), PCA.TransformationType.ROTATION)
        val completeOutput = outputMatrix.getArray()[0]
        if (completeOutput.size() > maxOutputDimensions) {
            val trimmedOutput = DoubleArray(maxOutputDimensions)
            for (ix in trimmedOutput.indices) {
                trimmedOutput[ix] = completeOutput[ix]
            }
            return trimmedOutput
        } else {
            return completeOutput
        }
    }
}