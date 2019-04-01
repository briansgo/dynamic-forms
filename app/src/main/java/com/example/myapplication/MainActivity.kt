package com.example.myapplication

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.rx2.Rx2Apollo
import com.apollographql.apollo.sample.FirstQuery
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class MainActivity : AppCompatActivity() {

    //private lateinit var apolloClient: MyApolloClient
    private var firstQuery = FirstQuery.builder().build()
    val xd = getAllFormOutline()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        print("apolloCall: $xd")

    }

    private fun getAllFormOutline(): ApolloCall<FirstQuery.Data>{
        val apolloCall = MyApolloClient.getMyApolloClient().query(firstQuery)

        val observable1 = Rx2Apollo.from(apolloCall)

        return apolloCall

    }

}

