package com.sumit.letsbrowse.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import com.sumit.letsbrowse.R
import com.sumit.letsbrowse.activity.MainActivity
import com.sumit.letsbrowse.databinding.FragmentBrowseBinding
import java.io.ByteArrayOutputStream

//
class BrowseFragment(private var uplink: String) : Fragment() {

     lateinit var  binding: FragmentBrowseBinding
    var favIcon: Bitmap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_browse, container, false)
        binding = FragmentBrowseBinding.bind(view)


        return view
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()

        MainActivity.tabbtn.text = MainActivity.tabsList.size.toString()
        MainActivity.tabsList[MainActivity.myPager.currentItem].name = binding.webView.url.toString()

        // for downloading file using external download manager
        binding.webView.setDownloadListener { url, _, _, _, _ ->
                  startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)))  }

        val mainRef = requireActivity() as MainActivity

        mainRef.binding.refershBtn.visibility = View.VISIBLE
        mainRef.binding.refershBtn.setOnClickListener {
            binding.webView.reload()
        }

        // webView is to load web sites in android which provide all the basic function to websites like zoom, load url.
        binding.webView.apply {

            webViewClient =  object: WebViewClient(){     // to display chrome tabs inside the app only
                override fun onLoadResource(view: WebView?, url: String?) {
                    super.onLoadResource(view, url)
                if(mainRef.isDesktop){
                    view?.evaluateJavascript("document.querySelector('meta[name=\"viewport\"]').setAttribute('content'," +
                        " 'width=1024px, initial-scale=' + (document.documentElement.clientWidth / 1024));", null)

                }
                }

                override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {    // method provide url of current search
                    super.doUpdateVisitedHistory(view, url, isReload)
                mainRef.binding.topSearchBar.setText(url) //
                    MainActivity.tabsList[MainActivity.myPager.currentItem].name = binding.webView.url.toString()
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    mainRef.binding.progressBar.progress=0
                    mainRef.binding.progressBar.visibility = View.VISIBLE
                    if(url!!.contains("you",false)){
                        mainRef.binding.root.transitionToEnd()
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    mainRef.binding.progressBar.visibility=View.GONE
                    binding.webView.zoomOut()
                }

            }
            webChromeClient =object : WebChromeClient(){  // to display chrome tabs inside the app only

                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    super.onShowCustomView(view, callback)
                    binding.webView.visibility=View.GONE
                    binding.cutomView.visibility=View.VISIBLE
                    binding.cutomView.addView(view)
                    mainRef.binding.root.transitionToEnd()
                }

                override fun onHideCustomView() {
                    super.onHideCustomView()
                    binding.webView.visibility=View.VISIBLE
                    binding.cutomView.visibility=View.GONE

                }

                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    mainRef.binding.progressBar.progress =newProgress
                }

                // method used to set website icon on top left
                override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                    super.onReceivedIcon(view, icon)
                    favIcon = icon
                    try {
                        mainRef.binding.webIcon.setImageBitmap(icon)
                        MainActivity.bookmarkIndex = mainRef.isBookmark(view?.url!!)
                        if (MainActivity.bookmarkIndex!=-1){
                            //  ------>    convert bitmap image to bytearray;  ------->
                            val array = ByteArrayOutputStream()
                            icon!!.compress(Bitmap.CompressFormat.PNG,100,array)
                        MainActivity.bookmarkList[MainActivity.bookmarkIndex].image = array.toByteArray()
                        }
                    } catch (e: Exception){

                    }


                }


            }
            settings.setSupportZoom(true)              // support zoom on webpage
         settings.displayZoomControls =false
        settings.builtInZoomControls =true
       settings.javaScriptEnabled=true               // for animated content
           if(URLUtil.isValidUrl(uplink)){
               loadUrl(uplink)
           } else if(uplink.contains(".com",true)){
               loadUrl(uplink)
           } else{
               loadUrl("https://www.google.com/search?q=$uplink")          // to load url inside Review
           }

             }
        binding.webView.setOnTouchListener { _, motionEvent ->
            mainRef.binding.root.onTouchEvent(motionEvent)
            return@setOnTouchListener false
        }
    }

    override fun onPause() {
        super.onPause()
        (requireActivity() as MainActivity).saveBookMark()

    binding.webView.apply {
        clearHistory()
        clearMatches()
        clearFormData()
        clearSslPreferences()
        clearCache(true)

        CookieManager.getInstance().removeAllCookies(null)
        WebStorage.getInstance().deleteAllData()
    }
    }

}
