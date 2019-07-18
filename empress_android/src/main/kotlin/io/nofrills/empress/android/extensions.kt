package io.nofrills.empress.android

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import io.nofrills.empress.DefaultEmpressApi
import io.nofrills.empress.DefaultEmpressBackend
import io.nofrills.empress.Empress
import io.nofrills.empress.EmpressApi

private const val DEFAULT_EMPRESS_ID = "default"

fun <Event, Patch : Any, Request> FragmentActivity.empress(
    empress: Empress<Event, Patch, Request>,
    id: String = DEFAULT_EMPRESS_ID
): EmpressApi<Event, Patch> {
    return getEmpressInstance(id, empress, supportFragmentManager)
}

fun <Event, Patch : Any, Request> Fragment.empress(
    empress: Empress<Event, Patch, Request>,
    id: String = DEFAULT_EMPRESS_ID
): EmpressApi<Event, Patch> {
    return getEmpressInstance(id, empress, childFragmentManager)
}

private fun <Event, Patch : Any, Request> getEmpressInstance(
    id: String,
    empress: Empress<Event, Patch, Request>,
    fragmentManager: FragmentManager
): EmpressApi<Event, Patch> {
    val fragmentTag = "io.nofrills.empress.fragment-$id"
    val fragment: EmpressFragment<Event, Patch> =
        fragmentManager.findFragmentByTag(fragmentTag) as EmpressFragment<Event, Patch>?
            ?: EmpressFragment<Event, Patch>().also {
                fragmentManager.beginTransaction().add(it, fragmentTag).commitNow()
            }
    fragment.initialize { storedModel -> DefaultEmpressBackend(empress, storedModel) }
    return DefaultEmpressApi(fragment.empressBackend)
}