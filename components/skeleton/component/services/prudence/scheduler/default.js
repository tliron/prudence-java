
var scheduler = new Packages.it.sauronsoftware.cron4j.Scheduler()
component.context.attributes.put('com.threecrickets.prudence.scheduler', scheduler)

var crontab = sincerity.container.getFile('component', 'crontab')
if (crontab.exists() && !crontab.directory) {
	scheduler.scheduleFile(crontab)
}

initializers.push(function() {
	scheduler.start()
})
