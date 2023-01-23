/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gmillz.compose.settings.util

import android.content.Context
import android.widget.EdgeEffect
import androidx.core.os.BuildCompat

/**
 * Extension of [EdgeEffect] to allow backwards compatibility
 */
open class EdgeEffectCompat(context: Context?) : EdgeEffect(context) {
    override fun getDistance(): Float {
        return if (BuildCompat.isAtLeastS()) super.getDistance() else 0f
    }

    override fun onPullDistance(deltaDistance: Float, displacement: Float): Float {
        return if (BuildCompat.isAtLeastS()) {
            super.onPullDistance(deltaDistance, displacement)
        } else {
            onPull(deltaDistance, displacement)
            deltaDistance
        }
    }
}