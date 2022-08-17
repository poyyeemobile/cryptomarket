package com.metadisk.cryptomarket.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.metadisk.cryptomarket.data.model.Coin
import com.metadisk.cryptomarket.data.util.Resource
import com.metadisk.cryptomarket.domain.usecase.GetCoinsUsecase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CoinViewModel(
    private val app: Application,
    private val getCoinsUsecase: GetCoinsUsecase
) : AndroidViewModel(app) {

    val coins: MutableLiveData<Resource<List<Coin>>> = MutableLiveData()


    fun getCoins(vs_currency: String, per_page: Int, page: Int) = viewModelScope.launch(Dispatchers.IO) {
        coins.postValue(Resource.Loading())
        try {
            if (isNetworkAvailable(app)) {

                val apiResult = getCoinsUsecase.execute(vs_currency, per_page, page)
                coins.postValue(apiResult)
            } else {
                coins.postValue(Resource.Error("Internet is not available"))
            }

        } catch (e: Exception) {
            coins.postValue(Resource.Error(e.message.toString()))
        }

    }


    private fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false

    }

}