package me.lovesasuna.bilibililive.util

import sun.misc.Unsafe
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.net.URL

object MethodHandleUtil {
    private var UNSAFE: Unsafe
    private var LOOKUP: MethodHandles.Lookup
    private var METHODTYPE: MethodType
    private var LOADER: ClassLoader

    init {
        try {
            LOADER = ClassLoader.getSystemClassLoader()
            val theUnsafe = Unsafe::class.java.getDeclaredField("theUnsafe")
            theUnsafe.isAccessible = true
            UNSAFE = theUnsafe.get(null) as Unsafe
            METHODTYPE = MethodType.methodType(Void::class.java, URL::class.java)
            MethodHandles.lookup()
            val lookupField = MethodHandles.Lookup::class.java.getDeclaredField("IMPL_LOOKUP")
            val lookupBase = UNSAFE.staticFieldBase(lookupField)
            val lookupOffset = UNSAFE.staticFieldOffset(lookupField)
            LOOKUP = UNSAFE.getObject(lookupBase, lookupOffset) as MethodHandles.Lookup
        } catch (t: Throwable) {
            throw  IllegalStateException("Unsafe not found")
        }
    }

    fun getHandle(obj: Any, methodName: String, rtype: Class<*>, vararg ptypes: Class<*>): MethodHandle {
        val methodType = MethodType.methodType(rtype, ptypes)
        return LOOKUP.findVirtual(obj.javaClass, methodName, methodType).bindTo(obj)
    }
}