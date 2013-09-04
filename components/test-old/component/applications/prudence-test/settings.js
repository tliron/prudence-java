
Sincerity.Objects.merge(app.settings, {
	description: {
		name: 'Prudence Test',
		description: 'A test application for Prudence',
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
	
	uploads: {
		root: 'uploads',
		sizeThreshold: '0kb'
	},
	
	mediaTypes: {
		php: 'text/html'
	}
})
