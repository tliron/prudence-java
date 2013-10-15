//
// This file is part of the Prudence Foundation Library
//
// Copyright 2009-2013 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.gnu.org/copyleft/lesser.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

document.require('/sincerity/templates/')

var Prudence = Prudence || {}

/**
 * 
 * @namespace
 * 
 * @author Tal Liron
 */
Prudence.Assets = Prudence.Assets || function() {
	/** @exports Public as Prudence.Assets */
	var Public = {}
    
    Public.template = '{base}/{name}?_={digest}'
    
    Public.getDigest = function(name) {
        Public.initialize()
    	return digests.get(String(name))
    }
    
    Public.getURL = function(name, conversation) {
    	return Sincerity.Templates.cast(Public.template, {
    		base: conversation.base,
    		name: name,
    		digest: Public.getDigest(name)
    	})
    }
    
    Public.initialize = function() {
        if (!Sincerity.Objects.exists(digests)) {
        	digests = application.globals.get('prudence.digests')
        	if (!Sincerity.Objects.exists(digests)) {
	        	digests = new java.util.Properties()
	    	    var file = new java.io.File(application.root, 'digests.conf')
	    	    var reader = new java.io.FileReader(file)
	    	    try {
	    	    	digests.load(reader)
	    	    }
	    	    finally {
	    	    	reader.close()
	    	    }
	    	    digests = application.getGlobal('prudence.digests', digests)
        	}
        }
    }

	//
	// Private
	//
    
    var digests
    
	return Public
}()
