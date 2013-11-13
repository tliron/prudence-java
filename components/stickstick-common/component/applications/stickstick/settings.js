
app.settings = {
	description: {
		name: 'Stickstick',
		description: 'A demo application for Prudence',
		author: 'Three Crickets',
		owner: 'Prudence'
	},

	errors: {
		debug: true,
		homeUrl: 'http://threecrickets.com/prudence/',
		contactEmail: 'info@threecrickets.com'
	},
	
	code: {
		libraries: ['libraries'],
		defrost: true,
		minimumTimeBetweenValidityChecks: '1s',
		defaultDocumentName: 'default',
		defaultExtension: 'js',
		defaultLanguageTag: 'javascript',
		sourceViewable: true
	},
	
	compression: {
		sizeThreshold: '1kb',
		exclude: []
	},
	
	uploads: {
		root: 'uploads',
		sizeThreshold: '0kb'
	},
	
	mediaTypes: {
		php: 'text/html'
	}
}

app.globals = {
	stickstick: {
		backend: 'h2',
		username: 'root',
		password: 'root',
		host: '',
		database: String(Sincerity.Container.getFileFromHere('data', 'stickstick')),
		log: String(sincerity.container.getLogsFile('stickstick.log')) // this is only used by Python 
	}
}
