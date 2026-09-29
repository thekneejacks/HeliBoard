# Copyright (C) 2013 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

LATIN_IME_JNI_SRC_FILES := \
    com_android_inputmethod_keyboard_ProximityInfo.cpp \
    jni_common.cpp

LATIN_IME_CORE_SRC_FILES := \
    $(addprefix suggest/core/layout/, \
        additional_proximity_chars.cpp \
        proximity_info.cpp \
        proximity_info_params.cpp \
        proximity_info_state.cpp \
        proximity_info_state_utils.cpp) \
    $(addprefix utils/, \
        autocorrection_threshold_utils.cpp \
        char_utils.cpp \
        log_utils.cpp \
        time_keeper.cpp)

LATIN_IME_CORE_SRC_FILES_BACKWARD_V402 := \

LATIN_IME_CORE_SRC_FILES += $(LATIN_IME_CORE_SRC_FILES_BACKWARD_V402)

LATIN_IME_CORE_TEST_FILES := \
    defines_test.cpp \
    utils/autocorrection_threshold_utils_test.cpp \
    utils/char_utils_test.cpp \
    utils/int_array_view_test.cpp \
    suggest/core/layout/geometry_utils_test.cpp \
    suggest/core/layout/normal_distribution_2d_test.cpp \
    suggest/policyimpl/utils/damerau_levenshtein_edit_distance_policy_test.cpp \
    utils/time_keeper_test.cpp
