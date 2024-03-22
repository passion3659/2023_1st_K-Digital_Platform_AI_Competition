package com.example.kdigitalaicompetition

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter

class SlidePagerAdapter(private val slideImages: IntArray, private val slideTexts: Array<String>) :
    PagerAdapter() {

    override fun getCount(): Int {
        return slideImages.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val context = container.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.slide_layout, container, false)

        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val descriptionTextView = view.findViewById<TextView>(R.id.descriptionTextView)

        imageView.setImageResource(slideImages[position])
        descriptionTextView.text = slideTexts[position]

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}