package com.example.hammami.presentation.ui.features.client

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.hammami.R
import com.example.hammami.databinding.FragmentAboutUsBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

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

        //da fare ("Non scaricare subito il pdf ma prima lo visualizza e poi permette il download")
        binding.sestaLista.setOnClickListener {
            downloadPdf()
        }
    }

    private fun downloadPdf() {
        val fileName = "CatalogoServiziHammami.pdf"
        val file = File(requireContext().filesDir, fileName)

        if (!file.exists()) {
            try {
                val inputStream: InputStream = requireContext().assets.open(fileName)
                val outputStream = FileOutputStream(file)
                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }
                outputStream.close()
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        val uri: Uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }

}