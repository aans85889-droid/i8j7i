package com.example.music

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SheelahAdapter(
    private val sheelahList: List<Sheelah>,
    private val onItemClick: (Int) -> Unit // دالة تنفذ عند النقر على الشيلة
) : RecyclerView.Adapter<SheelahAdapter.SheelahViewHolder>() {

    // كلاس داخلي يمسك عناصر واجهة السطر الواحد (ViewHolder)
    class SheelahViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivArtistIcon: ImageView = itemView.findViewById(R.id.ivArtistIcon)
        val tvSheelahTitle: TextView = itemView.findViewById(R.id.tvSheelahTitle)
        val tvSingerName: TextView = itemView.findViewById(R.id.tvSingerName)
        val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SheelahViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sheelah, parent, false)
        return SheelahViewHolder(view)
    }

    override fun onBindViewHolder(holder: SheelahViewHolder, position: Int) {
        val sheelah = sheelahList[position]

        // تعبئة البيانات في الشاشة
        holder.tvSheelahTitle.text = sheelah.title
        holder.tvSingerName.text = sheelah.singer
        holder.tvDuration.text = sheelah.duration
        holder.ivArtistIcon.setImageResource(sheelah.imageResId)

        // برمجة النقر على السطر للانتقال للمشغل
        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount(): Int = sheelahList.size
}