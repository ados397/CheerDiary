package com.ados.cheerdiary.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.cheerdiary.R
import com.ados.cheerdiary.databinding.SelectAppDialogBinding
import com.ados.cheerdiary.model.AppDTO

class SelectAppDialog(context: Context) : Dialog(context), OnItemClickListener {

    lateinit var binding: SelectAppDialogBinding

    private val layout = R.layout.select_app_dialog
    var recyclerViewAdapter: RecyclerViewAdapterAppList? = null
    var selectedAppName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SelectAppDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)

        //var rootView = binding.root.rootView
        //recyclerView = rootView.findViewById(R.id.rv_app_list!!)as RecyclerView
        //recyclerView.layoutManager = GridLayoutManager(context, 3)
        //val spaceDecoration = VerticalSpaceItemDecoration(10)
        //recyclerView.addItemDecoration(spaceDecoration)

        binding.rvAppList.layoutManager = GridLayoutManager(context, 3)

        var people : ArrayList<AppDTO> = arrayListOf()
        for (i in 0..30) {
            people.add(AppDTO(false, "com.iloen.melon", "멜론", "https://play-lh.googleusercontent.com/GweSpOJ7p8RZ0lzMDr7sU0x5EtvbsAubkVjLY-chdyV6exnSUfl99Am0g8X0w_a2Qo4=s180-rw"))
        }



        recyclerViewAdapter = RecyclerViewAdapterAppList(people, this)
        binding.rvAppList.adapter = recyclerViewAdapter

        /*var firestore = FirebaseFirestore.getInstance()
        firestore?.collection("people")?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!
                people.add(person)
            }
            recyclerview_img.adapter = RecyclerViewAdapterImageSelect(people, this)
        }
            ?.addOnFailureListener { exception ->

            }*/





    }

    override fun onItemClick(item: AppDTO, position: Int) {
        recyclerViewAdapter?.selectItem(position)
        selectedAppName = item.appName.toString()
    }

}