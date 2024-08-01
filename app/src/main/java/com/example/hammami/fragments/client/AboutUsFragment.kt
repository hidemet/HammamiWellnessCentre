package com.example.hammami.fragments.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.viewpager2.widget.ViewPager2
import com.example.hammami.R
import com.example.hammami.adapters.AboutUsViewpagerAdapter
import com.example.hammami.databinding.FragmentAboutUsBinding

class AboutUsFragment: Fragment(R.layout.fragment_about_us) {

    private lateinit var binding: FragmentAboutUsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutUsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.primaLista.setOnClickListener{
            if (binding.infoView.visibility == View.GONE) {
                binding.infoView.visibility = View.VISIBLE
            } else {
                binding.infoView.visibility = View.GONE
            }
        }

        binding.secondaLista.setOnClickListener{
            if (binding.sezioneTeamPrimaRiga.visibility == View.GONE && binding.sezioneTeamSecondaRiga.visibility == View.GONE) {
                binding.sezioneTeamPrimaRiga.visibility = View.VISIBLE
                binding.sezioneTeamSecondaRiga.visibility = View.VISIBLE
            } else {
                binding.sezioneTeamPrimaRiga.visibility = View.GONE
                binding.sezioneTeamSecondaRiga.visibility = View.GONE
            }
        }

        binding.terzaLista.setOnClickListener{
            if (binding.dispGiorni.visibility == View.GONE) {
                binding.dispGiorni.visibility = View.VISIBLE
            } else {
                binding.dispGiorni.visibility = View.GONE
            }
        }

        binding.quartaLista.setOnClickListener{
            if (binding.contenutoQuartaLista.visibility == View.GONE) {
                binding.contenutoQuartaLista.visibility = View.VISIBLE
            } else {
                binding.contenutoQuartaLista.visibility = View.GONE
            }
        }

        binding.quintaLista.setOnClickListener{
            if (binding.dispContatti.visibility == View.GONE) {
                binding.dispContatti.visibility = View.VISIBLE
            } else {
                binding.dispContatti.visibility = View.GONE
            }
        }

        /*
        binding.quintaLista.setOnClickListener{
            if (binding.dispContatti.visibility == View.GONE) {
                binding.dispContatti.visibility = View.VISIBLE
            } else {
                binding.dispContatti.visibility = View.GONE
            }
        }

         */
    }
}
