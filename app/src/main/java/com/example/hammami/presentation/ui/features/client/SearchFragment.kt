package com.example.hammami.presentation.ui.features.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.hammami.R
import com.example.hammami.databinding.FragmentSearchBinding

class SearchFragment: Fragment(R.layout.fragment_search) {

    private lateinit var binding: FragmentSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.Benessere.setOnClickListener{
            Navigation.findNavController(view)
                .navigate(R.id.action_searchFragment_to_catalogo_servizi_benessere)
        }

        binding.Estetica.setOnClickListener{
            Navigation.findNavController(view)
                .navigate(R.id.action_searchFragment_to_catalogo_servizi_estetica)
        }

        binding.Massaggi.setOnClickListener{
            Navigation.findNavController(view)
                .navigate(R.id.action_searchFragment_to_catalogo_servizi_massaggi)
        }

    }

    /*
    private lateinit var binding: FragmentServiziBenessereBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentServiziBenessereBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoriesFragments = arrayListOf<Fragment>(
            BenessereFragment()
        )

        val viewPager2Adapter =
            BenessereViewpagerAdapter(categoriesFragments, childFragmentManager, lifecycle)
        binding.viewpagerBenessere.orientation = ViewPager2.ORIENTATION_VERTICAL
        binding.viewpagerBenessere.adapter = viewPager2Adapter
    }
     */

}