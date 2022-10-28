package com.sumit.letsbrowse.activity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintJob
import android.print.PrintManager
import android.view.Gravity
import android.view.WindowManager
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.sumit.letsbrowse.R
import com.sumit.letsbrowse.activity.MainActivity.Companion.myPager
import com.sumit.letsbrowse.activity.MainActivity.Companion.tabbtn
import com.sumit.letsbrowse.adapter.TabsAdapter
import com.sumit.letsbrowse.databinding.ActivityMainBinding
import com.sumit.letsbrowse.databinding.BookmarkDialogBinding
import com.sumit.letsbrowse.databinding.CustomFeatureBinding
import com.sumit.letsbrowse.databinding.TabsViewBinding
import com.sumit.letsbrowse.fragments.BrowseFragment
import com.sumit.letsbrowse.fragments.HomeFragment
import com.sumit.letsbrowse.model.BookMark
import com.sumit.letsbrowse.model.Tabs
import java.io.ByteArrayOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

      lateinit var binding: ActivityMainBinding
   private  var printJob: PrintJob?= null
    var isDesktop: Boolean = false
    companion object {
        var bookmarkList: ArrayList<BookMark> = ArrayList()
        var tabsList: ArrayList<Tabs> = ArrayList()
        private var isFullscreen: Boolean = false
        var bookmarkIndex: Int = -1
        lateinit var myPager: ViewPager2
        lateinit var tabbtn: MaterialTextView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

      // full-screen for notch display
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getAllBookMarks()

        tabsList.add(Tabs("Home",HomeFragment()))
        //tabsList.add(BrowseFragment("https://www.google.com"))
        binding.myPager.adapter = TabsAdapter(supportFragmentManager, lifecycle)
          //  checkForInternet(this)
        binding.myPager.isUserInputEnabled = false   // no swiping feature in between fragments
        myPager = binding.myPager
       tabbtn  =binding.tabsbtn
        initializeView()
       // changeFullscreen(true)
    }
  // Adapter class for VIEW-PAGER ....view-pager is used to combined fragments
    private inner class TabsAdapter(fa: FragmentManager, lc: Lifecycle) : FragmentStateAdapter(fa, lc) {
        override fun getItemCount(): Int = tabsList.size

        override fun createFragment(position: Int): Fragment = tabsList[position].fragment
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBackPressed() {         // handled back button functionality
        var frag: BrowseFragment? =null
        try {
            frag = tabsList[binding.myPager.currentItem].fragment as BrowseFragment  // if curnt fragment is not of brserfrag then frag=null
        } catch (e: Exception){}
        when{
            frag?.binding?.webView?.canGoBack() == true -> frag.binding.webView.goBack()  //  chk if we have opened multiple web page

            binding.myPager.currentItem!=0 ->  {                     // if crnt != homefrag(0) then remove curnt frag.
                tabsList.removeAt(binding.myPager.currentItem)
                binding.myPager.adapter?.notifyDataSetChanged()
               binding.myPager.currentItem = tabsList.size -1
            }
            else -> super.onBackPressed()

        }
    }



   @SuppressLint("ResourceAsColor", "SetTextI18n")
   private fun initializeView(){

       binding.tabsbtn.setOnClickListener {

           val tabview = layoutInflater.inflate(R.layout.tabs_view,binding.root,false)
           val tabBinding = TabsViewBinding.bind(tabview)
               val tabdialog = MaterialAlertDialogBuilder(this,R.style.roundCornerDialog)
                   .setView(tabview)
                   .setTitle("Select Tab")
                   .setPositiveButton("Home"){self,_ ->
                      changeTabs("Home",HomeFragment())
                      self.dismiss() }

                   .setNeutralButton("Google"){self,_ ->
                       changeTabs("Google",BrowseFragment("www.google.com"))
                   self.dismiss()
                   }
                   .create()

           tabBinding.tabRV.setHasFixedSize(true)
           tabBinding.tabRV.layoutManager  =LinearLayoutManager(this)
           tabBinding.tabRV.adapter = TabsAdapter(this,tabdialog)

       tabdialog.show()

           val pbtn = tabdialog.getButton(AlertDialog.BUTTON_POSITIVE)
           val nbtn = tabdialog.getButton(AlertDialog.BUTTON_NEUTRAL)
           pbtn.isAllCaps = false
           nbtn.isAllCaps = false

           pbtn.setTextColor(Color.BLACK)
           nbtn.setTextColor(Color.BLACK)

           // not working
           pbtn.setCompoundDrawablesWithIntrinsicBounds( ResourcesCompat.getDrawable(resources, R.drawable.ic_home_btn, theme)
               , null, null, null)
           nbtn.setCompoundDrawablesWithIntrinsicBounds( ResourcesCompat.getDrawable(resources, R.drawable.ic_add, theme)
               , null, null, null)
       }

        binding.seetingBtn.setOnClickListener {
            var frag: BrowseFragment? =null
            try {
                frag = tabsList[binding.myPager.currentItem].fragment as BrowseFragment  // if curnt fragment is not of brserfrag then frag=null
            } catch (e: Exception){}


            val view = layoutInflater.inflate(R.layout.custom_feature,binding.root,false)

            val dialogBinding = CustomFeatureBinding.bind(view)
                val dialog = MaterialAlertDialogBuilder(this).setView(view).create()
           dialog.window?.apply {
               attributes.gravity = Gravity.BOTTOM
               attributes.y = 50
               setBackgroundDrawable(ColorDrawable(0xffffffff.toInt()))
           }
            dialog.show()
           frag?.let {
               bookmarkIndex = isBookmark(it.binding.webView.url!!)
               if(bookmarkIndex!=-1){
                   dialogBinding.bookmarkBtn.apply {
                           setIconTintResource(R.color.cool_blue)
                           setTextColor(ContextCompat.getColor(this@MainActivity,R.color.cool_blue))
                       } }  }

            if(isFullscreen){
                dialogBinding.fullscreenbtn.apply {
                    setIconTintResource(R.color.cool_blue)
                    setTextColor(ContextCompat.getColor(this@MainActivity,R.color.cool_blue))
                } }

            dialogBinding.backBtn.setOnClickListener {
                onBackPressed()
            }
            dialogBinding.frwrdBtn.setOnClickListener {
                frag?.apply {
                if(binding.webView.canGoForward()){
                    binding.webView.goForward()
                }
                } }
            dialogBinding.saveBtn.setOnClickListener {
                dialog.dismiss()
           if(frag!=null){
               saveAsPdf( frag.binding.webView)
           } else{
               Snackbar.make(binding.root,"Can't Download webPage",3000).show()
           }
            }
            dialogBinding.fullscreenbtn.setOnClickListener {
                it as MaterialButton
                if(isFullscreen){
                    changeFullscreen(false)
                    it.setIconTintResource(R.color.black)
                    it.setTextColor(ContextCompat.getColor(this@MainActivity,R.color.black))

                    isFullscreen =false
                } else{
                    changeFullscreen(true)
                    it.setIconTintResource(R.color.cool_blue)
                    it.setTextColor(ContextCompat.getColor(this@MainActivity,R.color.cool_blue))
                isFullscreen =true
                }
                dialog.dismiss()
            }
            dialogBinding.desktopBtn.setOnClickListener {
                it as MaterialButton
                frag?.binding?.webView?.apply {

                    if(isDesktop){
                       settings.userAgentString = null
                        it.setIconTintResource(R.color.black)
                        it.setTextColor(ContextCompat.getColor(this@MainActivity,R.color.black))
                        isDesktop=false
                    } else{
                        it.setIconTintResource(R.color.cool_blue)
                        it.setTextColor(ContextCompat.getColor(this@MainActivity,R.color.cool_blue))

                        // need to change user agent to cnahnge mobile site to Desktop-site
                        // every OS has it's own user-agent
                    frag.binding.webView.settings.userAgentString= "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36"
                     frag.binding.webView.settings.useWideViewPort
                        evaluateJavascript("document.querySelector('meta[name=\"viewport\"]').setAttribute('content'," +
                            " 'width=1024px, initial-scale=' + (document.documentElement.clientWidth / 1024));", null)

                        isDesktop=true
                    }
                    reload()
                    dialog.dismiss()
                }
            }
            dialogBinding.bookmarkBtn.setOnClickListener {
                frag?.let {


                    val viewB = layoutInflater.inflate(R.layout.bookmark_dialog,binding.root,false)
                 if(bookmarkIndex==-1){
                     val bBinding = BookmarkDialogBinding.bind(viewB)
                     val dialogB = MaterialAlertDialogBuilder(this)
                         .setView(viewB)
                         .setTitle("Add Bookmark")
                         .setMessage("Url:${it.binding.webView.url!!} ")
                         .setPositiveButton("Add"){self,_ ->
                             try {
                                 val array = ByteArrayOutputStream()
                                 it.favIcon?.compress(Bitmap.CompressFormat.PNG,100,array)
                                 bookmarkList.add(BookMark(bBinding.bookmarkTitle.text.toString(), it.binding.webView.url!!,array.toByteArray()))
                             } catch (e: Exception){
                                 bookmarkList.add(BookMark(bBinding.bookmarkTitle.text.toString(), it.binding.webView.url!!))
                             }
                         }
                         .setNegativeButton("Cancel"){self,_ -> self.dismiss()}
                         .create()
                     bBinding.bookmarkTitle.setText(it.binding.webView.title)
                     dialogB.show() }
                    else{
                        val dialogB = MaterialAlertDialogBuilder(this)
                         .setTitle("Remove Bookmark")
                         .setMessage("Url: ${it.binding.webView.url!!}")
                         .setPositiveButton("Remove"){self,_ ->
                             bookmarkList.removeAt(bookmarkIndex)
                         }
                         .setNegativeButton("Cancel"){self,_ -> self.dismiss()}
                         .create()
                     dialogB.show()

                 }
                    dialog.dismiss()
                }
            }

        }
   }

    override fun onResume() {
        super.onResume()
        printJob?.let {
        when{
            it.isCompleted -> Snackbar.make(binding.root,"Successfull->${it.info.label}",3000).show()
            it.isFailed -> Snackbar.make(binding.root,"Failed->${it.info.label}",3000).show()

        }
        }
    }

    private fun saveAsPdf(web: WebView){
       val pm = getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "${URL(web.url).host}_${SimpleDateFormat("HH:mm d_MMM_yy",Locale.ENGLISH).format(Calendar.getInstance().time)}"
      val printAdapter = web.createPrintDocumentAdapter(jobName)
        val printAttributes = PrintAttributes.Builder()
        printJob= pm.print(jobName,printAdapter,printAttributes.build())
    }

    private fun changeFullscreen(enable: Boolean){
        if(enable){
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, binding.root).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }else{
            WindowCompat.setDecorFitsSystemWindows(window, true)
            WindowInsetsControllerCompat(window, binding.root).show(WindowInsetsCompat.Type.systemBars())
        }
    }
    public fun isBookmark( url: String): Int {
        bookmarkList.forEachIndexed { index, bookMark ->
            if(bookMark.url==url) return index
        }
        return -1
    }

    fun saveBookMark(){
        //  save data to storage using shared preference
        val editor = getSharedPreferences("BookMark", MODE_PRIVATE).edit()
        val data = GsonBuilder().create().toJson(bookmarkList)
        editor.putString("bookmarkList",data)
        editor.apply()
    }

    fun getAllBookMarks(){
        // reterive data from storage using shared preference
        bookmarkList = ArrayList()
        val editor = getSharedPreferences("BookMark", MODE_PRIVATE)
       val data = editor.getString("bookmarkList",null)
        if(data!=null){
            val list: ArrayList<BookMark>  =GsonBuilder().create().fromJson(data, object: TypeToken<ArrayList<BookMark>>(){}.type)
            bookmarkList.addAll(list)
        }
    }
}

@SuppressLint("NotifyDataSetChanged")
fun changeTabs(url: String, fragment: Fragment) { // method to add and change fragment when search query is performed
    MainActivity.tabsList.add(Tabs(url,fragment))

    //  Snack.make(binding!!.root, "fragment added ${tabsList.size}",1000).show()
    myPager.adapter?.notifyDataSetChanged()
    myPager.currentItem= MainActivity.tabsList.size-1
      tabbtn.text = MainActivity.tabsList.size.toString()

}

fun checkForInternet(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    } else {
        @Suppress("DEPRECATION") val networkInfo =
            connectivityManager.activeNetworkInfo ?: return false
        @Suppress("DEPRECATION")
        return networkInfo.isConnected
    }
}
