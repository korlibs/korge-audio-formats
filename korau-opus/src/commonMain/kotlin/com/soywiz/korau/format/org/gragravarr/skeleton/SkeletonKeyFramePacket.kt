/*
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
package korlibs.audio.format.org.gragravarr.skeleton

import korlibs.audio.format.org.gragravarr.ogg.*

/**
 * Keyframe Index Packets, used for seeking within the
 * main streams
 */
class SkeletonKeyFramePacket : OggStreamAudioData, SkeletonPacket {
	constructor(pkt: OggPacket) : super(pkt) {}
	constructor(data: ByteArray) : super(data) {}

	// TODO Parse
}
