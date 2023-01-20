package id.onklas.app.utils

const val NETWORK_LOADING = 0
const val NETWORK_SUCCESS = 1
const val NETWORK_FAILED = 2

data class NetworkState(var state: Int = NETWORK_LOADING, var msg: String = "Loading data") {
    companion object {
        val FAILED = NetworkState(NETWORK_FAILED, "Failed to load")
        val SUCCESS = NetworkState(NETWORK_SUCCESS, "Success")
    }
}