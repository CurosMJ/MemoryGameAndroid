package `in`.curos.memorygame

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : Activity() {

    var recyclerView: RecyclerView? = null
    val URL = "https://shopicruit.myshopify.com/admin/products.json?page=1&access_token=c32313df0d0ef512ca64d5b336a0d7c6"
    var images : MutableList<String> = ArrayList()
    var scoreText: TextView? = null

    var gridSizeSpinner: Spinner? = null
    var matchSizeSpinner: Spinner? = null

    var grid = 5
    var matchesToFind = 2
    var scoreToWin = 10


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.shuffle).setOnClickListener {
            this.setup()
        }
        recyclerView = findViewById(R.id.recycler_view)
        scoreText = findViewById(R.id.score_text)
        gridSizeSpinner = findViewById<Spinner>(R.id.grid_size_spinner)
        ArrayAdapter.createFromResource(this, R.array.grid_size_options, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                gridSizeSpinner?.adapter = adapter
                gridSizeSpinner?.setSelection(1)
            }
        matchSizeSpinner = findViewById<Spinner>(R.id.match_size_spinner)
        ArrayAdapter.createFromResource(this, R.array.match_size_options, android.R.layout.simple_spinner_item)
                .also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    matchSizeSpinner?.adapter = adapter
                }

        gridSizeSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, id: Long) {
                changeParameters()
            }
        }

        matchSizeSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, id: Long) {
                changeParameters()
            }
        }


        var request = Request.Builder().url(URL).build()

        var client = OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    handleFailure()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        handleData(response)
                    } else {
                        handleFailure()
                    }
                }
            }
        })
    }

    fun changeParameters() {
        var gridSize = gridSizeSpinner?.selectedItem.toString()
        var matchSize = matchSizeSpinner?.selectedItem.toString()

        grid = gridSize[0].toString().toInt()
        matchesToFind = matchSize[0].toString().toInt()
        scoreToWin = grid.times(grid).div(matchesToFind).minus(2)
        this.setup()
    }

    fun handleFailure() {
        Log.e("MJ", "FAIL")
        scoreText?.setText("Please check your internet connection")
    }

    fun handleData(response: Response) {
        var json = JSONObject(response.body?.string())
        var products = json.getJSONArray("products")

        var list : ArrayList<String> = ArrayList()

        for (i in 0 until products.length()) {
            var src = products.getJSONObject(i).getJSONObject("image").getString("src")
            list.add(src)
        }
        images = list
        this.setup()
    }

    fun setup() {
        var list = images.toList().toMutableList()

        if (list.size == 0) {
            return
        }

        recyclerView?.visibility = View.VISIBLE
        scoreText?.setText("Score: 0 / ${scoreToWin}")

        list.shuffle()
        list = list.slice(0 until Math.ceil(grid.times(grid).div(matchesToFind).toDouble()).toInt()) as ArrayList<String>
        while (list.size < grid * grid) {
            list.addAll(list)
        }
        list = list.slice(0 until grid * grid) as ArrayList<String>
        list.shuffle()

        var mAdapter = CardGridAdapter(this, list, matchesToFind, {adapter: CardGridAdapter ->
            var score = adapter.solvedCards.size / matchesToFind
            scoreText?.setText("Score: ${score.toString()}  / ${scoreToWin}")

            if (score >= scoreToWin) {
                scoreText?.setText("You Win!!!")
                recyclerView?.visibility = View.INVISIBLE
                Toast.makeText(this, "You win!! Press Shuffle to play again", Toast.LENGTH_LONG).show()
                adapter.disableGame()
            }
        })
        recyclerView?.apply {
            adapter = mAdapter
            layoutManager = GridLayoutManager(context, grid)
        }
    }
}
