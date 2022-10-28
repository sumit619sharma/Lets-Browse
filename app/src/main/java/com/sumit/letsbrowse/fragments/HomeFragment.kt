package com.sumit.letsbrowse.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sumit.letsbrowse.R
import com.sumit.letsbrowse.activity.BookMarkActivity
import com.sumit.letsbrowse.activity.MainActivity
import com.sumit.letsbrowse.activity.changeTabs
import com.sumit.letsbrowse.activity.checkForInternet
import com.sumit.letsbrowse.adapter.BookMarkAdapter
import com.sumit.letsbrowse.databinding.FragmentHomeBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

       lateinit var binding: FragmentHomeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        binding = FragmentHomeBinding.bind(view)

        return view
    }

    override fun onResume() {
        super.onResume()
        MainActivity.tabsList[MainActivity.myPager.currentItem].name = "Home"
        MainActivity.tabbtn.text = MainActivity.tabsList.size.toString()

var main = requireActivity() as MainActivity
        main.binding.refershBtn.visibility =View.GONE
        main.binding.topSearchBar.setText("")                 // whenever comes to homefrag set topsrchBar and search-view  to("") .
        binding.searchView.setQuery("",false)
       main.binding.webIcon.setImageResource(R.drawable.ic_search_bar)

        // method passed the search url to changeTabs  Method which handle further operation
        // method passed the search url to brsr-fragment to load this url
        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(result: String?): Boolean {
                if(checkForInternet(requireContext())){
                   changeTabs(result!!, BrowseFragment(result))
                }else{
                  Snackbar.make(binding.root, "NO Internet 🙄🙄🙄",1000).show()
                }

              return true
            }
            override fun onQueryTextChange(p0: String?): Boolean = false
        })

    main.goBtn.setOnClickListener {
        val find = main.topSearchBar.text.toString()
        if(checkForInternet(requireContext())){
           changeTabs(find, BrowseFragment(find))
        }else{
            Snackbar.make(binding.root, "NO Internet 🙄🙄🙄",1000).show()
        }
    }


        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.setItemViewCacheSize(5)
        binding.recyclerView.layoutManager  =GridLayoutManager(requireContext(),5)
        binding.recyclerView.adapter = BookMarkAdapter(requireContext(),false)

        if(MainActivity.bookmarkList.size<1){
            binding.viewAllBtn.visibility = View.GONE
        }
        viewAllBtn.setOnClickListener {

            val intent = Intent(requireContext(), BookMarkActivity::class.java)
            startActivity(intent)
        }

    }


}
