/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.schnettler.repo.mapping

interface Mapper<F, T> {
    suspend fun map(from: F): T
}

interface IndexedMapper<F, T> {
    suspend fun map(index: Int, from: F): T
}

fun <F, T> Mapper<F, T>.forLists(): suspend (List<F>) -> List<T> {
    return { list -> list.map { item -> map(item) } }
}

fun <F, T> IndexedMapper<F, T>.forLists(): suspend (List<F>) -> List<T> {
    return { list -> list.mapIndexed { index, item -> map(index, item) } }
}

fun <F, T1, T2> pairMapperOf(
    firstMapper: Mapper<F, T1>,
    secondMapper: Mapper<F, T2>
): suspend (List<F>) -> List<Pair<T1, T2>> {
    return { from ->
        from.map { firstMapper.map(it) to secondMapper.map(it) }
    }
}

fun <F, T1, T2> pairMapperOf(
    firstMapper: Mapper<F, T1>,
    secondMapper: IndexedMapper<F, T2>
): suspend (List<F>) -> List<Pair<T1, T2>> {
    return { from ->
        from.mapIndexed { index, value ->
            firstMapper.map(value) to secondMapper.map(index, value)
        }
    }
}

fun <F, T> Mapper<F, T>.toLambda(): suspend (F) -> T {
    return { map(it) }
}