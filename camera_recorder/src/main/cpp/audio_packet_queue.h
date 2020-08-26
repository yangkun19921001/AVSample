#ifndef LIVE_AUDIO_PACKETQUEUE_H
#define LIVE_AUDIO_PACKETQUEUE_H

#include <pthread.h>

typedef unsigned char byte;
typedef struct AudioPacket {
	short * buffer;
	byte* data;
	int size;
	float position;
	long frameNum;

    AudioPacket() {
		buffer = NULL;
		data = NULL;
		size = 0;
		position = -1;
	}
	~AudioPacket() {
		if (NULL != buffer) {
			delete[] buffer;
			buffer = NULL;
		}
		if (NULL != data) {
			delete[] data;
			data = NULL;
		}
	}
} tyAudioPacket;

typedef struct AudioPacketList {
	AudioPacket *pkt;
	struct AudioPacketList *next;
    AudioPacketList(){
		pkt = NULL;
		next = NULL;
	}
} AudioPacketList;
inline void buildPacketFromBuffer(AudioPacket * audioPacket, short* samples, int sampleSize) {
	short* packetBuffer = new short[sampleSize];
	if (NULL != packetBuffer) {
		memcpy(packetBuffer, samples, sampleSize * 2);
		audioPacket->buffer = packetBuffer;
		audioPacket->size = sampleSize;
	} else {
		audioPacket->size = -1;
	}
}
class AudioPacketQueue {
public:
    AudioPacketQueue();
    AudioPacketQueue(const char* queueNameParam);
	~AudioPacketQueue();

	void init();
	void flush();
	int put(AudioPacket* audioPacket);

	/* return < 0 if aborted, 0 if no packet and > 0 if packet.  */
	int get(AudioPacket **audioPacket, bool block);

	int size();

	void abort();

private:
	AudioPacketList* mFirst;
    AudioPacketList* mLast;
	int mNbPackets;
	bool mAbortRequest;
	pthread_mutex_t mLock;
	pthread_cond_t mCondition;
	const char* queueName;
};

#endif // LIVE_AUDIO_PACKETQUEUE_H
