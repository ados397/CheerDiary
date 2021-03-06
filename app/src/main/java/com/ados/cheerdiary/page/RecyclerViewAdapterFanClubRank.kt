package com.ados.cheerdiary.page

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.ados.cheerdiary.R
import com.ados.cheerdiary.databinding.ListItemFanClubRankBinding
import com.ados.cheerdiary.model.FanClubDTO
import com.bumptech.glide.Glide
import java.text.DecimalFormat

class RecyclerViewAdapterFanClubRank(private val items: ArrayList<FanClubDTO>, var clickListener: OnFanClubRankItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterFanClubRank.ViewHolder>() {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")
    var context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = ListItemFanClubRankBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.initializes(items[position], clickListener)

        items[position].let { item ->
            with(holder) {
                if (imgRank != null) {
                    when (position) {
                        0 -> {
                            Glide.with(imgRank.context).asBitmap().load(R.drawable.award_01).fitCenter().into(holder.imgRank)
                            imgRank.visibility = View.VISIBLE
                            rank.visibility = View.GONE
                            cardView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.rank1))
                            //imgRank.setImageResource(R.drawable.award_01)
                            //cardView.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFA2"))
                        }
                        1 -> {
                            Glide.with(imgRank.context).asBitmap().load(R.drawable.award_02).fitCenter().into(holder.imgRank)
                            imgRank.visibility = View.VISIBLE
                            rank.visibility = View.GONE
                            cardView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.rank2))
                            //imgRank.setImageResource(R.drawable.award_02)
                            //cardView.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#C4EDFF"))
                        }
                        2 -> {
                            Glide.with(imgRank.context).asBitmap().load(R.drawable.award_03).fitCenter().into(holder.imgRank)
                            imgRank.visibility = View.VISIBLE
                            rank.visibility = View.GONE
                            cardView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.rank3))
                            //imgRank.setImageResource(R.drawable.award_03)
                            //cardView.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFE0EC"))
                        }
                        else -> {
                            imgRank.visibility = View.GONE
                            rank.visibility = View.VISIBLE
                            rank.text = "${position+1}"
                            cardView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.white))
                            //cardView.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                        }
                    }
                }

                var imageID = itemView.context.resources.getIdentifier(item.imgSymbol, "drawable", itemView.context.packageName)
                if (imgSymbol != null && imageID > 0) {
                    //iconImage?.setImageResource(item)
                    Glide.with(imgSymbol.context)
                        .asBitmap()
                        .load(imageID) ///feed in path of the image
                        .fitCenter()
                        .into(holder.imgSymbol)
                }

                name.text = "${item.name}"
                level.text = "Lv. ${item.level}"
                exp.text = "${decimalFormat.format(item.exp)}"
                count.text = "${item.count}/${item.countMax}"
                description.text = item.description

                if (item.isSelected) {
                    mainLayout.setBackgroundColor(Color.parseColor("#BBD5F8"))
                } else {
                    mainLayout.setBackgroundColor(Color.parseColor("#FFFFFF"))
                }
            }
        }
    }

    // ?????? ????????? ????????? ????????? ?????? ????????? ???????????? false ??????, ???????????? ???????????? ?????? ??? true ??????
    fun selectItem(position: Int) : Boolean {
        return if (items[position].isSelected) {
            items[position].isSelected = false
            notifyDataSetChanged()
            false
        } else {
            for (item in items) {
                item.isSelected = false
            }
            items[position].isSelected = true
            notifyDataSetChanged()
            true
        }
    }

    inner class ViewHolder(private val viewBinding: ListItemFanClubRankBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var imgRank = viewBinding.imgRank
        var imgSymbol = viewBinding.imgSymbol
        var rank = viewBinding.textRank
        var name = viewBinding.textName
        var level = viewBinding.textLevel
        var exp = viewBinding.textExp
        var count = viewBinding.textCount
        var description = viewBinding.textDescription
        var cardView = viewBinding.cardView
        var mainLayout = viewBinding.layoutMain

        fun initializes(item: FanClubDTO, action:OnFanClubRankItemClickListener) {
            viewBinding.layoutMain.setOnClickListener {
                action.onItemClick(item, adapterPosition)
            }
        }
    }
}

interface OnFanClubRankItemClickListener {
    fun onItemClick(item: FanClubDTO, position: Int)
}