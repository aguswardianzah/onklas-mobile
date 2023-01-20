package id.onklas.app.utils

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import id.onklas.app.R

class NoFilterArrayAdapter(
    context: Context,
    var values: List<String> = emptyList(),
    layout: Int = R.layout.text_item
) :
    ArrayAdapter<String>(context, layout, values) {

    private val filterThatDoesNothing = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            results.values = values
            results.count = values.size
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter = filterThatDoesNothing
}
