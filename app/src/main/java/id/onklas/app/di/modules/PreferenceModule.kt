package id.onklas.app.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.Reusable

@Module
object PreferenceModule {

    @Provides
    @Reusable
    @JvmStatic
    fun instance(context: Context) = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
}