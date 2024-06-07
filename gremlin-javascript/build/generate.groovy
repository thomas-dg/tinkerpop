/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.tinkerpop.gremlin.language.translator.GremlinTranslator
import org.apache.tinkerpop.gremlin.language.translator.Translator
import org.apache.tinkerpop.gremlin.language.corpus.FeatureReader

import java.nio.file.Paths

// file is overwritten on each generation
radishGremlinFile = new File("${projectBaseDir}/gremlin-javascript/src/main/javascript/gremlin-javascript/test/cucumber/gremlin.js")

// assumes globally unique scenario names for keys with list of Gremlin traversals as they appear
gremlins = FeatureReader.parseGrouped(Paths.get("${projectBaseDir}", "gremlin-test", "src", "main", "resources", "org", "apache", "tinkerpop", "gremlin", "test", "features").toString())

radishGremlinFile.withWriter('UTF-8') { Writer writer ->
    writer.writeLine('/*\n' +
            '* Licensed to the Apache Software Foundation (ASF) under one\n' +
            '* or more contributor license agreements.  See the NOTICE file\n' +
            '* distributed with this work for additional information\n' +
            '* regarding copyright ownership.  The ASF licenses this file\n' +
            '* to you under the Apache License, Version 2.0 (the\n' +
            '* "License"); you may not use this file except in compliance\n' +
            '* with the License.  You may obtain a copy of the License at\n' +
            '* \n' +
            '* http://www.apache.org/licenses/LICENSE-2.0\n' +
            '* \n' +
            '* Unless required by applicable law or agreed to in writing,\n' +
            '* software distributed under the License is distributed on an\n' +
            '* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY\n' +
            '* KIND, either express or implied.  See the License for the\n' +
            '* specific language governing permissions and limitations\n' +
            '* under the License.\n' +
            '*/\n')

    writer.writeLine("\n\n//********************************************************************************")
    writer.writeLine("//* Do NOT edit this file directly - generated by build/generate.groovy")
    writer.writeLine("//********************************************************************************\n\n")

    writer.writeLine(
                    'import * as graphTraversalModule from \'../../lib/process/graph-traversal.js\';\n' +
                    'import * as traversalModule from \'../../lib/process/traversal.js\';\n' +
                    'import { TraversalStrategies, VertexProgramStrategy, OptionsStrategy, PartitionStrategy, ReadOnlyStrategy, SeedStrategy, SubgraphStrategy, ProductiveByStrategy } from \'../../lib/process/traversal-strategy.js\';\n' +
                    'const __ = graphTraversalModule.statics;\n' +
                    'const Barrier = traversalModule.barrier\n' +
                    'const Cardinality = traversalModule.cardinality\n' +
                    'const CardinalityValue = graphTraversalModule.CardinalityValue;\n' +
                    'const Column = traversalModule.column\n' +
                    'const Direction = {\n' +
                    '    BOTH: traversalModule.direction.both,\n' +
                    '    IN: traversalModule.direction.in,\n' +
                    '    OUT: traversalModule.direction.out,\n' +
                    '    from_: traversalModule.direction.out,\n' +
                    '    to: traversalModule.direction.in\n' +
                    '};\n' +
                    'const IO = traversalModule.IO;\n' +
                    'const DT = traversalModule.dt;\n' +
                    'const Merge = traversalModule.merge;\n' +
                    'const P = traversalModule.P;\n' +
                    'const Pick = traversalModule.pick\n' +
                    'const Pop = traversalModule.pop\n' +
                    'const Order = traversalModule.order\n' +
                    'const Operator = traversalModule.operator\n' +
                    'const Scope = traversalModule.scope\n' +
                    'const T = traversalModule.t\n' +
                    'const TextP = traversalModule.TextP\n' +
                    'const WithOptions = traversalModule.withOptions\n'
    )

    // some traversals may require a static translation if the translator can't handle them for some reason
    def staticTranslate = [:]
    // SAMPLE: g_injectXnull_nullX: "    g_injectXnull_nullX: [function({g}) { return g.inject(null,null) }], ",

    writer.writeLine('const gremlins = {')
    gremlins.each { k,v ->
        // skipping lambdas until we decide for sure that they are out in 4.x
        if (v.any { it.contains('l1')} || v.any { it.contains('pred1')} || v.any { it.contains('Lambda')}) {
            writer.writeLine("    '${k}': [],  // skipping as it contains a lambda")
        } else if (staticTranslate.containsKey(k)) {
            writer.writeLine(staticTranslate[k])
        } else {
            writer.write("    ")
            writer.write(k)
            writer.write(": [")
            def collected = v.collect { GremlinTranslator.translate(it, Translator.JAVASCRIPT) }
            def uniqueBindings = collected.collect { it.getParameters() }.flatten().unique()
            def gremlinItty = collected.iterator()
            while (gremlinItty.hasNext()) {
                def t = gremlinItty.next()
                writer.write("function({g")
                if (!uniqueBindings.isEmpty()) {
                    writer.write(", ")
                    writer.write(uniqueBindings.join(", "))
                }
                writer.write("}) { return ")
                writer.write(t.getTranslated())
                writer.write(" }")
                if (gremlinItty.hasNext()) writer.write(', ')
            }
            writer.writeLine('], ')
        }
    }
    writer.writeLine('}\n')

    writer.writeLine('export const gremlin = gremlins')
}


