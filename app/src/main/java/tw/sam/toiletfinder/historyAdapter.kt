package tw.sam.toiletfinder

import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.testcardview.view.*

/**
 * Created by sam on 2018/1/6.
 */
class historyAdapter(val history:MutableList<Toilet>) :RecyclerView.Adapter<historyAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int):ViewHolder{
        val view = LayoutInflater.from(parent.context).inflate(R.layout.testcardview,parent,false)
        return ViewHolder(view)
    }
    override fun getItemCount(): Int {
        return history.size
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.render(history.get(position))
    }

    inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        fun render(toilet:Toilet){
            itemView.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    var toiletInfo = Intent(itemView.context, DataInfo::class.java) //做包裹
                    toiletInfo.putExtra("toilet", toilet)
                    toiletInfo.putExtra("Name", toilet.Name)
                    itemView.context.startActivity(toiletInfo) //送進詳細資訊欄
                }
            })

            itemView.hiName.text = toilet.Name
            itemView.hiaddress.text = toilet.Address
            itemView.higrade.text= toilet.Grade

            var typeOptions = arrayOf(0,0,0,0) //男廁女廁無障礙親子廁
            when(toilet.Type){
                "男女","男女廁","混合廁" -> {typeOptions[0]=1
                    typeOptions[1]=1}
                "男","男廁"-> typeOptions[0]=1
                "女","女廁"-> typeOptions[1]=1
                "無障礙","無障礙廁"-> typeOptions[2]=1
                "親子","親子廁"-> typeOptions[3]=1
            }
            itemView.cardicon.setImageResource(toilet.getIcon())

            if(typeOptions[0]==0) itemView.himale.visibility=View.GONE else itemView.himale.visibility=View.VISIBLE
            if(typeOptions[1]==0) itemView.hifemale.visibility=View.GONE else itemView.hifemale.visibility=View.VISIBLE
            if(typeOptions[2]==0) itemView.hirestroom.visibility=View.GONE else itemView.hirestroom.visibility=View.VISIBLE
            if(typeOptions[3]==0) itemView.hichildroom.visibility=View.GONE else itemView.hichildroom.visibility=View.VISIBLE
            var sum=0
            for(i in typeOptions){
                sum+=i
            }
            if(sum==0) itemView.hiempty.visibility=View.VISIBLE else itemView.hiempty.visibility=View.GONE

            if(toilet.Attr.isEmpty()){
                itemView.hitype.visibility= View.GONE
            }
            else {
                itemView.hitype.visibility= View.VISIBLE
                itemView.hitype.text= toilet.Attr
            }
        }
    }
}