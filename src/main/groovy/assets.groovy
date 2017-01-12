/**
 * Adapted from <a href="https://gist.github.com/kellyrob99/2d1483828c5de0e41732327ded3ab224">kellyrob99/repoAssetLister.groovy</a>
 */

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.sonatype.nexus.repository.storage.Asset
import org.sonatype.nexus.repository.storage.Query
import org.sonatype.nexus.repository.storage.StorageFacet

def request = new JsonSlurper().parseText(args)
assert request.repoName: 'repoName parameter is required'
assert request.startDate: 'startDate parameter is required'

log.info("Gathering Asset list for repository: ${request.repoName} as of startDate: ${request.startDate}")

def repo = repository.repositoryManager.get(request.repoName)
StorageFacet storageFacet = repo.facet(StorageFacet)
def tx = storageFacet.txSupplier().get()

tx.begin()

Iterable<Asset> assets = tx.findAssets(Query.builder().where('last_updated > ').param(request.startDate).build(), [repo])
def assetPaths = assets.collect { "${it.name()}" }

tx.commit()

return JsonOutput.toJson([
        repository: "${repo.url}",
        assets    : assetPaths
])
