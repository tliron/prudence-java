
document.require('/dispatched/groovy/person/')

resources = [
	'person': this.class.classLoader.loadClass('Person').newInstance()
]
