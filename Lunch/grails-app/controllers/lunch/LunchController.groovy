package lunch

import grails.converters.JSON
import groovy.json.JsonSlurper

import org.apache.commons.logging.LogFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.support.PropertiesLoaderUtils

class LunchController {

	def index() {
	}

	def first() {

		def suitable = getWeather()

		if(suitable != null) {
			def question = getQuestion("$suitable.0")
			if(question) {
				def result = [
					status: true,
					type: 'question',
					question: question
				]
				render result as JSON
			}
		}

		def result = [
			status: false,
			message: message(code:'message.error')
		]

		render result as JSON
	}

	def answer() {
		def history = params.history

		if(history) {
			history += ",${params.id}"
		} else {
			history = params.id
		}
		
		def next = getNext(params.id)

		if(next) {
			def result = [
				status: true,
				type: 'question',
				history: history,
				question: getQuestion(next)
			]

			render result as JSON
		} else {
			def room = getRoom(history)
			if(room) {
				def result = [
					status: true,
					type: 'room',
					room: room.name,
					roomList: room.roomList
				]
				render result as JSON
			}

			def result = [
				status: false,
				message: message(code:'message.room.empty')
			]
			render result as JSON
		}
	}

	def change() {
		if(params.roomList) {
			def rooms = params.roomList.split(' ') as ArrayList

			if(rooms) {
				def index = (rooms.size() * Math.random()) as int
				def room = rooms.get(index)
				rooms.remove(index)

				if(room) {
					def roomList = ''
					rooms.each { roomList += "$it " }

					def result = [
						status: true,
						type: 'room',
						room: room,
						roomList: roomList.trim()
					]
					render result as JSON
					return
				}
			}
		}

		def result = [
			status: false,
			message: message(code:'message.room.empty')
		]
		render result as JSON
	}

	def getRoom(String history) {
		def questions = PropertiesLoaderUtils.loadProperties(new ClassPathResource("questions.properties"))
		def rooms = PropertiesLoaderUtils.loadProperties(new ClassPathResource("rooms.properties"))

		if(!history) {
			return null
		}

		def ranges = [:]

		def options = history.split(',')

		options.each {
			def qid = it.substring(0, it.lastIndexOf('.'))
			def theme = questions["theme.$qid"]
			def range = questions["option.range.$it"]

			if(ranges[theme]) {
				ranges[theme] = ranges[theme].intersect(range.split(',') as ArrayList)
			} else {
				ranges[theme] = range.split(',') as ArrayList
			}
		}

		def roomResult
		def roomDistance = [] as ArrayList
		def roomTaste = [] as ArrayList
		
		if(ranges.distance) {
			ranges.distance.each {
				if(roomDistance) {
					roomDistance.addAll(rooms["distance.$it"].split(',') as ArrayList)
				} else {
					roomDistance = rooms["distance.$it"].split(',') as ArrayList
				}
			}
		} else {
			def index = 0
			while(true) {
				def rs = rooms["distance.$index"]
				if(rs == null) {
					break
				}
				roomDistance.addAll(rs.split(',') as ArrayList)
				index++
			}
		}
		
		if(ranges.taste) {
			ranges.taste.each {
				if(roomTaste) {
					roomTaste.addAll(rooms["taste.$it"].split(',') as ArrayList)
				} else {
					roomTaste = rooms["taste.$it"].split(',') as ArrayList
				}
			}
		} else {
			def index = 0
			while(true) {
				def rs = rooms["taste.$index"]
				if(rs == null) {
					break
				}
				roomTaste.addAll(rs.split(','))
				index++
			}
		}
		
		roomResult = roomDistance.intersect(roomTaste)

		if(!roomResult) {
			return null
		}

		def index = (roomResult.size() * Math.random()) as int
		def room = roomResult.get(index)
		roomResult.remove(index)


		def roomList = ''
		roomResult.each { roomList += "$it " }

		return [name: room, roomList: roomList.trim()]
	}

	def getNext(String id) {
		def questions = PropertiesLoaderUtils.loadProperties(new ClassPathResource("questions.properties"))

		return questions["option.next.$id"]
	}

	def getQuestion(String id) {
		def questions = PropertiesLoaderUtils.loadProperties(new ClassPathResource("questions.properties"))
		def topic = questions["topic.$id"]

		if(!topic) {
			return null
		}
		def theme = questions["theme.$id"]
		def text = message(code:"question.$topic")
		def options = []
		def item = 0
		while(true) {
			def range = questions.getProperty("option.range.$id.${item}")

			if(!range) {
				break
			}
			range = range.split(',') as ArrayList
			def option = [:]
			option.id = "$id.$item"
			option.text = message(code:"question.${topic}.${item}")
			options.add(option)
			item++
		}
		def question = [:]
		question.text = text
		question.options = options

		return question
	}

	def getWeather() {
		def classpath = new ClassPathResource("weather.properties")
		def weather = PropertiesLoaderUtils.loadProperties(classpath)
		def suitable = weather.suitable
		def time = weather.time
		def reload = !suitable || !time
		if(!reload) {
			reload = new Date().getTime() - Date.parse("yyyyMMddHHmmss", time).getTime() > 30*60*1000
		}

		if(reload) {
			def slurper = new JsonSlurper()
			def result = slurper.parse(new URL(weather.url), "UTF-8")
			result.any {
				result = it.value
				return true
			}
			
			if("ok".equalsIgnoreCase(result.status)) {
				log.info("NOW: ${result.now}")
				log.info("AQI: ${result.aqi}")
				
				def code =  result.now.cond.code[0]
				def temp = result.now.fl[0] as int
				def aqi = result.aqi.city.aqi[0] as int
				suitable = true
					
				if(!weather[code] as boolean
				|| temp < (weather.templower as int)
				|| temp > (weather.tempupper as int)
				|| aqi > (weather.aqiupper as int)) {
					suitable = false
				}

				weather.setProperty('time', new Date().format('yyyyMMddHHmmss'))
				weather.setProperty('suitable', suitable.toString())
				weather.store(new FileOutputStream(classpath.getFile()), '')
			} else {
				log.info("RESULT: $result")
				return null
			}
		}

		return suitable
	}
}
