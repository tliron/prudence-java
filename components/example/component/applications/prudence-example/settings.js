
/***
 * @swagger info
 * @title Prudence Example
 * @description The example application
 *              for the Prudence skeleton
 * @license GNU Lesser General Public License 3.0
 * @licenseUrl http://www.gnu.org/licenses/lgpl.html
 * @contact info@threecrickets.com
 */

app.settings = {
	description: {
		name: 'Prudence Example',
		description: 'The example application for the Prudence skeleton',
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
	
	templates: {
		debug: true
	},

	caching: {
		debug: true
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
